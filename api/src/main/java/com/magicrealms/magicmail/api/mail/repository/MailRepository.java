package com.magicrealms.magicmail.api.mail.repository;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.repository.BaseRepository;
import com.magicrealms.magiclib.common.store.MongoDBStore;
import com.magicrealms.magiclib.common.store.RedisStore;
import com.magicrealms.magiclib.common.utils.MongoDBUtil;
import com.magicrealms.magiclib.common.utils.RedissonUtil;
import com.magicrealms.magiclib.core.dispatcher.MessageDispatcher;
import com.magicrealms.magicmail.api.MagicMail;
import com.magicrealms.magicmail.api.exception.ReceiveException;
import com.magicrealms.magicmail.api.mail.AttachmentItem;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.api.mail.MailStatus;
import com.magicrealms.magicmail.api.util.PlayerInventoryUtil;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.magicrealms.magicmail.common.MagicMailConstant.*;

/**
 * @author Ryan-0916
 * @Desc 邮件数据存储类
 * @date 2025-05-17
 */
@SuppressWarnings("unused")
public class MailRepository extends BaseRepository<Mail> {

    public MailRepository(MongoDBStore mongoDBStore,
                          @Nullable RedisStore redisStore) {
        super(mongoDBStore, MAGIC_MAIL_TABLE_NAME, redisStore, Mail.class);
    }


    private void cacheMailBox(String key, List<Mail> mails) {
        getRedisStore().hSetObject(key, mails.stream()
                        .collect(Collectors.toMap(
                                Mail::getId,    // Key: Mail 的 ID
                                mail -> mail,   // Value: Mail 对象本身
                                (existing, replacement) -> existing,  // 如果 Key 冲突，保留旧值
                                LinkedHashMap::new  // 使用 LinkedHashMap 保持顺序
                        )), MagicMail.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Cache.Mail", 3600L, ParseType.LONG));
    }

    /**
     * 查询玩家邮箱内的全部邮件
     * @param player 玩家
     * @return 邮箱内的全部邮件
     */
    public List<Mail> queryMailBox(Player player) {
        String id = StringUtils.upperCase(player.getName());
        String key = String.format(MAGIC_MAIL_RECEIVED_MAILS, id);
        Optional<List<Mail>> cachedData = getRedisStore().hGetAllObject(key, Mail.class);
        if (cachedData.isPresent()) {
            return cachedData.get();
        }
        List<Mail> mails = new ArrayList<>();
        try (MongoCursor<Document> cursor = getMongoDBStore().find(getTableName(), Filters
                        .regex("receiver_name", "^" + Pattern.quote(id) + "$", "i"))) {
            while (cursor.hasNext()) {
                mails.add(MongoDBUtil.toObject(cursor.next(), Mail.class));
            }
        }
        if (!mails.isEmpty()) {
            cacheMailBox(key, mails);
        }
        return mails;
    }

    /**
     * 发送邮件至玩家邮箱
     * @param mail 邮件内容
     */
    public void sendMail(Mail mail) {
        String key = String.format(MAGIC_MAIL_RECEIVED_MAILS, StringUtils
                .upperCase(mail.getReceiverName()));
        insert(mail);
        if (getRedisStore().exists(key)) { getRedisStore().hSetObject(key, mail.getId(), mail, MagicMail.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Cache.Mail", 3600L, ParseType.LONG)); }
        MessageDispatcher.getInstance().sendBungeeMessage(MagicMail.getInstance().getRedisStore(),
                BUNGEE_CHANNEL,
                mail.getReceiverName(),
                MagicMail.getInstance().getConfigManager().getYmlValue(YML_LANGUAGE, "PlayerMessage.Success.ReceiveMail")
        );
    }

    private Consumer<Mail> getReceiveConsumer(String... attachmentItemIds) throws ReceiveException {
        return mail -> {
            if (!mail.isValid()) {
                throw new ReceiveException("邮件已经过期，无法领取");
            }
            if (mail.getStatus() != MailStatus.UNREAD) {
                throw new ReceiveException("邮件已被领取或已过期");
            }
            Set<String> mailAttachmentIds = mail.getAttachment().getPendingReceiptItems()
                    .stream()
                    .map(AttachmentItem::getId)
                    .collect(Collectors.toCollection(HashSet::new));
            Set<String> requestedIds = new HashSet<>(Arrays.asList(attachmentItemIds));
            if (requestedIds.size() > mailAttachmentIds.size() ||
                    !mailAttachmentIds.containsAll(requestedIds)) {
                throw new ReceiveException("附件物品状态异常，存在未知或已被领取的附件");
            }
            if (requestedIds.size() == mailAttachmentIds.size()) {
                mail.setStatus(MailStatus.READ);
            }
            mail.getAttachment().getItems().forEach(item -> {
                if (!item.isReceived() && requestedIds.contains(item.getId())) {
                    item.setReceived(true);
                }
            });
        };
    }

    /**
     * 领取邮件内全部物品
     * @param player 玩家
     * @param mailId 邮件编号
     */
    public void receiveMail(Player player, String mailId) {
        String upName = StringUtils.upperCase(player.getName());
        String key = String.format(MAGIC_MAIL_RECEIVED_MAILS, upName);
        String lockKey = String.format(MAGIC_MAIL_RECEIVE_LOCK, upName);
        RedissonUtil.doAsyncWithLock(getRedisStore(), lockKey, upName, 5000L, () ->
                processMailReceipt(player, mailId, key, null));
    }

    /**
     * 领取邮件内指定物品
     * @param player 玩家
     * @param mailId 邮件编号
     * @param attachmentItemIds 附件物品编号
     */
    public void receiveMail(Player player, String mailId, String... attachmentItemIds) {
        String upName = StringUtils.upperCase(player.getName());
        String key = String.format(MAGIC_MAIL_RECEIVED_MAILS, upName);
        String lockKey = String.format(MAGIC_MAIL_RECEIVE_LOCK, upName);
        RedissonUtil.doAsyncWithLock(getRedisStore(), lockKey, upName, 5000L, () -> processMailReceipt(player, mailId, key,
                new HashSet<>(Arrays.asList(attachmentItemIds))));
    }

    private void processMailReceipt(Player player, String mailId, String redisKey, Set<String> requestedItemIds) {
        Optional<Mail> mailOptional = queryMailBox(player).stream()
                .filter(m -> StringUtils.equalsIgnoreCase(player.getName(), m.getReceiverName())
                        && m.getStatus() == MailStatus.UNREAD
                        && StringUtils.equals(mailId, m.getId())
                        && m.isValid())
                .findFirst();
        if (mailOptional.isEmpty()) {
            sendPlayerMessage(player, "PlayerMessage.Error.NotFountUnreadMail");
            return;
        }
        Mail mail = mailOptional.get();
        List<AttachmentItem> attachmentItems = requestedItemIds == null
                ? mail.getAttachment().getPendingReceiptItems()
                : mail.getAttachment().getItems().stream()
                .filter(e -> requestedItemIds.contains(e.getId()))
                .toList();
        try {
            Consumer<Mail> receiveConsumer = getReceiveConsumer(
                    attachmentItems.stream().map(AttachmentItem::getId).toArray(String[]::new));
            boolean b = updateById(mailId, receiveConsumer);
            if (!b) {
                sendPlayerMessage(player, "PlayerMessage.Error.ReceiveException");
                getRedisStore().removeKey(redisKey);
                return;
            }
            receiveConsumer.accept(mail);
            getRedisStore().hSetObject(redisKey, mailId, mail, -1);
            PlayerInventoryUtil.givePlayerItems(player,
                    attachmentItems.stream().map(AttachmentItem::getItem).toList());
            sendPlayerMessage(player, "PlayerMessage.Success.Receive");
        } catch (ReceiveException e) {
            sendPlayerMessage(player, "PlayerMessage.Error.ReceiveException");
            getRedisStore().removeKey(redisKey);
        }
    }

    private void sendPlayerMessage(Player player, String messageKey) {
        MessageDispatcher.getInstance().sendMessage(
                MagicMail.getInstance(),
                player,
                MagicMail.getInstance().getConfigManager().getYmlValue(YML_LANGUAGE, messageKey)
        );
    }
}

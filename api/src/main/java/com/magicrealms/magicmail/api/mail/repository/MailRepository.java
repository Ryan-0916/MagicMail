package com.magicrealms.magicmail.api.mail.repository;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.repository.BaseRepository;
import com.magicrealms.magiclib.common.store.MongoDBStore;
import com.magicrealms.magiclib.common.store.RedisStore;
import com.magicrealms.magiclib.common.utils.MongoDBUtil;
import com.magicrealms.magicmail.api.MagicMail;
import com.magicrealms.magicmail.api.mail.Mail;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.magicrealms.magicmail.common.MagicMailConstant.MAGIC_MAIL_RECEIVED_MAILS;
import static com.magicrealms.magicmail.common.MagicMailConstant.YML_CONFIG;

/**
 * @author Ryan-0916
 * @Desc 邮件数据存储类
 * @date 2025-05-17
 */
public class MailRepository extends BaseRepository<Mail> {

    public MailRepository(MongoDBStore mongoDBStore,
                          String tableName,
                          @Nullable RedisStore redisStore) {
        super(mongoDBStore, tableName, redisStore, false, 0, Mail.class);
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
        if (!getRedisStore().exists(key)) { return; }
        getRedisStore().hSetObject(key, mail.getId(), mail, MagicMail.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Cache.Mail", 3600L, ParseType.LONG));
    }

}

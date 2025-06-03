package com.magicrealms.magicmail.api.mail;

import com.magicrealms.magiclib.common.adapt.UUIDFieldAdapter;
import com.magicrealms.magiclib.common.annotations.FieldId;
import com.magicrealms.magiclib.common.annotations.MongoField;
import com.magicrealms.magiclib.common.utils.IdGeneratorUtil;
import com.magicrealms.magicmail.api.mail.adapter.MailStatusFieldAdapter;
import lombok.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Ryan-0916
 * @Desc 邮件
 * @date 2025-05-17
 */
@Data
@Builder(builderClassName = "MailBuilder", toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class Mail {
    /* 编号 */
    @MongoField(id = @FieldId(enable = true))
    private String id;
    @MongoField(adapter = UUIDFieldAdapter.class)
    private UUID senderId;
    @MongoField
    private String senderName;
    @MongoField(adapter = UUIDFieldAdapter.class)
    private UUID receiverId;
    @MongoField
    private String receiverName;
    /* 标题 */
    @MongoField
    private String subject;
    /* 内容 */
    @MongoField
    private String content;
    /* 发送时间 */
    @MongoField
    private long sendTime;
    /* 有效期 ms， 0代表永久有效，如若3天内有效则填写 3 * 24 * 60 * 60 * 1000 */
    @MongoField
    private long expire;
    /* 状态 */
    @MongoField(adapter = MailStatusFieldAdapter.class)
    private MailStatus status;
    /* 附件 */
    @MongoField(recursive = true)
    private MailAttachment attachment;

    public static MailBuilder builder(@Nullable Player sender, Player receiver) {
        Objects.requireNonNull(receiver, "Receiver cannot be null");
        return new MailBuilder()
                .id(IdGeneratorUtil.getId())
                .receiverId(receiver.getUniqueId())
                .receiverName(receiver.getName())
                .status(MailStatus.UNREAD)
                .sendTime(System.currentTimeMillis())
                .attachment(MailAttachment.builder().build())
                .senderId(sender != null ? sender.getUniqueId() : null)
                .senderName(sender != null ? sender.getName() : null);
    }

    public static MailBuilder builder(Player receiver) {
        return builder(null, receiver);
    }

    public String getSenderName() {
        return senderName != null ? senderName : "System";
    }

    /* 是否有效 */
    public boolean isValid() {
        return expire == 0 || System.currentTimeMillis() <= (sendTime + expire);
    }


}

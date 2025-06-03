package com.magicrealms.magicmail.api.mail;

import com.magicrealms.magiclib.common.annotations.MongoField;
import com.magicrealms.magicmail.api.mail.adapter.BigDecimalFieldAdapter;
import com.magicrealms.magicmail.api.mail.adapter.AttachmentItemAdapter;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ryan-0916
 * @Desc 邮件附件
 * @date 2025-05-17
 */
@Data
@Builder(builderClassName = "MailAttachmentBuilder", toBuilder = true)
@AllArgsConstructor
@SuppressWarnings("unused")
public class MailAttachment {
    /* 点券 */
    @MongoField
    private int point;
    /* 金额 */
    @MongoField(adapter = BigDecimalFieldAdapter.class)
    private BigDecimal amount;
    /* 物品 */
    @MongoField(adapter = AttachmentItemAdapter.class)
    private List<AttachmentItem> items;

    public MailAttachment() {
        items = new ArrayList<>();
    }

    public static MailAttachmentBuilder builder() {
        return new MailAttachmentBuilder()
                .amount(BigDecimal.ZERO)
                .items(new ArrayList<>());
    }

    public List<AttachmentItem> getPendingReceiptItems() {
        return items.stream().filter(e -> !e.isReceived())
                .collect(Collectors.toList());
    }
}

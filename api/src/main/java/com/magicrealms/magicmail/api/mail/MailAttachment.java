package com.magicrealms.magicmail.api.mail;

import com.magicrealms.magiclib.common.annotations.MongoField;
import com.magicrealms.magicmail.api.mail.adapter.BigDecimalFieldAdapter;
import com.magicrealms.magicmail.api.mail.adapter.ItemsFieldAdapter;
import lombok.*;
import org.bukkit.inventory.ItemStack;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 邮件附件
 * @date 2025-05-17
 */
@Getter
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
    @MongoField(adapter = ItemsFieldAdapter.class)
    private List<ItemStack> items;

    public MailAttachment() {
        items = new ArrayList<>();
    }

    public static MailAttachmentBuilder builder() {
        return new MailAttachmentBuilder()
                .amount(BigDecimal.ZERO)
                .items(new ArrayList<>());
    }
}

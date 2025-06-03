package com.magicrealms.magicmail.api.mail;

import com.magicrealms.magiclib.common.utils.IdGeneratorUtil;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * @author Ryan-0916
 * @Desc 附件物品
 * @date 2025-06-02
 */
@Data
public class AttachmentItem {

    /* 物品编号 */
    private final String id;
    /* 物品 */
    private final ItemStack item;
    /* 是否领取 */
    private boolean received;

    public AttachmentItem(ItemStack item) {
        Objects.requireNonNull(item);
        this.id = IdGeneratorUtil.getId();
        this.item = item;
    }
}

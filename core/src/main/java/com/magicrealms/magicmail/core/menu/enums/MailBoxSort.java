package com.magicrealms.magicmail.core.menu.enums;

import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import static com.magicrealms.magicmail.common.MagicMailConstant.YML_MAILBOX_MENU;

/**
 * @author Ryan-0916
 * @Desc 邮箱排序方式
 * @date 2025-05-28
 */
@Getter
public enum MailBoxSort {

    /* 按收件时间从新到旧 (值: 0) */
    NEWEST(0, "NewestDisplay"),

    /* 按收件时间从旧到新 (值: 1) */
    OLDEST(1, "OldestDisplay");

    /* 枚举值对应的整数值 */
    private final int value;

    /* Menu YML 中对应的 Path */
    private final String path;

    MailBoxSort(int value, String path) {
        this.value = value;
        this.path = path;
    }

    /**
     * 获取下一个排序方式（循环切换）
     * @return 下一个排序方式枚举值
     */
    public MailBoxSort next() {
        MailBoxSort[] values = MailBoxSort.values();
        int nextOrdinal = (this.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }

    public ItemStack getItemSlot(char key) {
        String path = String.format("Icons.%s.%s", key, this.path);
        return ItemUtil.getItemStackByConfig(BukkitMagicMail.getInstance().getConfigManager(), YML_MAILBOX_MENU, path);
    }
}

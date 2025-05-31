package com.magicrealms.magicmail.core.menu.strategy;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.Tuple;
import com.magicrealms.magicmail.core.menu.MailboxMenu;
import com.magicrealms.magicmail.core.menu.enums.MailboxCategory;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ryan-0916
 * @Desc 分类标签动画
 * 采用策略模式
 * @date 2025-05-30
 */
public abstract class CategoryStrategy {

    protected final int ENABLE_OFFSET;

    protected final int UN_ENABLE_OFFSET;

    protected final MailboxMenu HOLDER;

    public CategoryStrategy(MailboxMenu holder) {
        this.ENABLE_OFFSET = holder.getPlugin().getConfigManager().getYmlValue(holder.getConfigPath(), "Setting.Category.Offset.Enable", 0, ParseType.INTEGER);
        this.UN_ENABLE_OFFSET = holder.getPlugin().getConfigManager().getYmlValue(holder.getConfigPath(), "Setting.Category.Offset.UnEnable", 0, ParseType.INTEGER);
        this.HOLDER = holder;
    }

    public void destroy() {}

    protected int calculateOffset(boolean enable) {
        return enable ? ENABLE_OFFSET : UN_ENABLE_OFFSET;
    }

    @Getter
    protected Tuple<Integer, Integer, Integer> offset;

    public abstract void categoryChange(MailboxCategory newCategory,
                                  @Nullable MailboxCategory oldCategory);

    public abstract void asyncUpdateTitle();

}

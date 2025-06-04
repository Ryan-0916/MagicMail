package com.magicrealms.magicmail.core.menu.strategy;

import com.magicrealms.magicmail.core.menu.MailboxMenu;
import com.magicrealms.magicmail.core.menu.enums.MailboxCategory;
import com.magicrealms.magicmail.core.utils.Tuple;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ryan-0916
 * @Desc 默认策略
 * @date 2025-05-30
 */
public class DefaultStrategy extends AbstractCategoryStrategy {

    public DefaultStrategy(MailboxMenu holder) {
        super(holder);
    }

    public void categoryChange(MailboxCategory newCategory,
                               @Nullable MailboxCategory oldCategory) {
        offset = Tuple.of(calculateOffset(newCategory == MailboxCategory.ALL),
                calculateOffset(newCategory == MailboxCategory.UNREAD),
                calculateOffset(newCategory == MailboxCategory.READ),
                calculateOffset(newCategory == MailboxCategory.EXPIRED));
    }

    @Override
    public void asyncUpdateTitle() {
        /* 调用邮箱的 updateTitle */
        HOLDER.updateTitle();
    }

}

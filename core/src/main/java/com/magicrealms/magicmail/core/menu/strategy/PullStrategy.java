package com.magicrealms.magicmail.core.menu.strategy;

import com.magicrealms.magiclib.common.utils.Quad;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import com.magicrealms.magicmail.core.menu.MailboxMenu;
import com.magicrealms.magicmail.core.menu.enums.MailboxCategory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ryan-0916
 * @Desc 抽拉策略
 * @date 2025-05-30
 */
public class PullStrategy extends AbstractCategoryStrategy {

    /* 动画时长 */
    private final int ANIMATION_TICK;
    /* 总偏移量 */
    private final int TOTAL_OFFSET;
    /* 每 Tick 的偏移量 */
    private final int STEEP_OFFSET;
    /* 偏移方向 正数往左，负数往右 */
    private final int DIRECTION;
    /* 任务 */
    private BukkitTask task;

    public PullStrategy(MailboxMenu menu, int animationTick) {
        super(menu);
        ANIMATION_TICK = animationTick;
        TOTAL_OFFSET = Math.abs(ENABLE_OFFSET - UN_ENABLE_OFFSET);
        STEEP_OFFSET = TOTAL_OFFSET / animationTick;
        DIRECTION =  (UN_ENABLE_OFFSET > ENABLE_OFFSET) ? 1 : -1;
    }

    public void categoryChange(MailboxCategory newCategory, @Nullable MailboxCategory oldCategory) {
        if (oldCategory == null || ANIMATION_TICK <= 0 || STEEP_OFFSET <= 0) {
            offset = Quad.of(calculateOffset(newCategory == MailboxCategory.ALL),
                    calculateOffset(newCategory == MailboxCategory.UNREAD),
                    calculateOffset(newCategory == MailboxCategory.READ),
                    calculateOffset(newCategory == MailboxCategory.EXPIRED)
            );
            return;
        }
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = createAnimationTask(newCategory, oldCategory)
                .runTaskTimerAsynchronously(BukkitMagicMail.getInstance(), 0, 1);
    }

    private BukkitRunnable createAnimationTask(MailboxCategory newCategory,
                                               MailboxCategory oldCategory) {
        return new BukkitRunnable() {
            /* 当前步数 */
            int step = 0;
            /* 当前偏移量 */
            int currentOffset = 0;
            @Override
            public void run() {
                currentOffset += STEEP_OFFSET;
                if(++step > ANIMATION_TICK || currentOffset >= TOTAL_OFFSET) {
                    restore();
                    return;
                }
                offset = Quad.of(calculateOffset(currentOffset, newCategory, oldCategory, MailboxCategory.ALL),
                        calculateOffset(currentOffset, newCategory, oldCategory, MailboxCategory.UNREAD),
                        calculateOffset(currentOffset, newCategory, oldCategory, MailboxCategory.READ),
                        calculateOffset(currentOffset, newCategory, oldCategory, MailboxCategory.EXPIRED));
                HOLDER.updateTitle();
            }

            public void restore() {
                cancel();
                offset = Quad.of(calculateOffset(newCategory == MailboxCategory.ALL),
                        calculateOffset(newCategory == MailboxCategory.UNREAD),
                        calculateOffset(newCategory == MailboxCategory.READ),
                        calculateOffset(newCategory == MailboxCategory.EXPIRED));
                HOLDER.updateTitle();
            }

            @Override
            public void cancel() {
                super.cancel();
                task = null;
            }
        };
    }

    private int calculateOffset(int currentOffset,
                                        MailboxCategory newCategory,
                                        MailboxCategory oldCategory,
                                        MailboxCategory checkCategory) {
        /* 偏移量 * 方位得出正确的偏移量 */
        int dynamicOffset = currentOffset * DIRECTION;
        return newCategory == checkCategory ? UN_ENABLE_OFFSET - dynamicOffset
                : oldCategory == checkCategory ? ENABLE_OFFSET + dynamicOffset
                : UN_ENABLE_OFFSET;
    }

    @Override
    public void asyncUpdateTitle() {
        if (task == null || task.isCancelled()) {
            /* 调用邮箱的 updateTitle */
            HOLDER.updateTitle();
        }
    }

    @Override
    public void destroy() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}

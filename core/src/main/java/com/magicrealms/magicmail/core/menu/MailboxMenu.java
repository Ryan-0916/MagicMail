package com.magicrealms.magicmail.core.menu;
import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.FormatUtil;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.MagicLib;
import com.magicrealms.magiclib.core.holder.PageMenuHolder;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.api.mail.MailStatus;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import com.magicrealms.magicmail.core.menu.enums.MailBoxSort;
import com.magicrealms.magicmail.core.menu.enums.MailboxCategory;
import com.magicrealms.magicmail.core.menu.listener.DataChangeListener;
import com.magicrealms.magicmail.core.menu.strategy.AbstractCategoryStrategy;
import com.magicrealms.magicmail.core.menu.strategy.DefaultStrategy;
import com.magicrealms.magicmail.core.menu.strategy.PullStrategy;
import com.magicrealms.magicmail.core.utils.LineBreakFormatter;
import com.magicrealms.magicplayer.api.MagicPlayerAPI;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.magicrealms.magicmail.common.MagicMailConstant.YML_LANGUAGE;
import static com.magicrealms.magicmail.common.MagicMailConstant.YML_MAILBOX_MENU;

/**
 * @author Ryan-0916
 * @Desc 收件箱菜单
 * @date 2025-05-26
 */
public class MailboxMenu extends PageMenuHolder implements DataChangeListener {
    /* Menu 按钮相关属性 Key */
    String ICON_DISPLAY = "Icons.%s.Display";
    /* 邮件数量字体占位符 */
    private final String MAIL_COUNT_FONT_FORMAT;
    /* 邮件数量变量 */
    private String countPapi;
    /* 邮件数量气泡变量*/
    private String countBubblePapi;
    /* 所有邮件 */
    private final List<Mail> MAILS;
    /* 每页显示邮件数量 */
    private final int PAGE_COUNT;
    /* 显示的邮件 */
    private List<Mail> data;
    /* 分类 */
    private MailboxCategory category = MailboxCategory.UNREAD;
    /* 排序 */
    private MailBoxSort sort = MailBoxSort.NEWEST;
    /* 分类动画策略 */
    private final AbstractCategoryStrategy categoryState;
    /* 邮箱图标缓存 */
    private final Map<String, ItemStack> mailIconCache = new HashMap<>();

    public MailboxMenu(Player player) {
        super(BukkitMagicMail.getInstance(), player, YML_MAILBOX_MENU, "A####BCDEF#IJJJJJKG#IJJJJJLH#IJJJJJ###IJJJJJM##IJJJJJN");
        this.MAIL_COUNT_FONT_FORMAT = String.format("<font:%s>", getPlugin().getConfigManager().getYmlValue(getConfigPath(),
                        "Setting.Count.Font")) + "%s</font>";
        this.MAILS = BukkitMagicMail.getInstance().getMailRepository().queryMailBox(player);
        this.PAGE_COUNT = StringUtils.countMatches(getLayout(), "I");
        int tick = getPlugin().getConfigManager().getYmlValue(getConfigPath(), "Setting.Category.Tick", 0, ParseType.INTEGER);
        this.categoryState = tick > 0 ? new PullStrategy(this, tick) :
                new DefaultStrategy(this);
        categoryState.categoryChange(category, null);
        this.data = MAILS.stream().filter(category::filter).collect(Collectors.toList());
        onDataChanged(true);
    }

    /**
     * 异步更新 Title， 由于 Title 可能出现动态效果
     * 考虑性能方面更新 Title 功能已被策略器代理
     * {@link AbstractCategoryStrategy#asyncUpdateTitle()}
     */
    @Override
    protected void asyncUpdateTitle() {
        categoryState.asyncUpdateTitle();
    }

    public void updateTitle() {
        super.asyncUpdateTitle();
    }

    @Override
    public void onDataChanged(boolean isInit) {
        sortData();
        /* 邮件数量变量 */
        this.countPapi = String.format(MAIL_COUNT_FONT_FORMAT, Math.min(data.size(), 99));
        /* 邮件气泡变量 */
        this.countBubblePapi = calculateCountBubble();
        /* 计算当前最大页数 */
        setMaxPage(PAGE_COUNT <= 0 || data.isEmpty() ? 1 :
                data.size() % PAGE_COUNT == 0 ?
                        data.size() / PAGE_COUNT : data.size() / PAGE_COUNT + 1);
        if (isInit) {
            asyncOpenMenu();
            return;
        }
        /* 释放缓存 */
        cleanItemCache();
        /* 回退至第一页 */
        goToFirstPage();
        /* 处理菜单 */
        handleMenu(getLayout());
        /* 异步修改菜单标题 */
        asyncUpdateTitle();
    }

    private void sortData() {
        switch (sort) {
            case NEWEST -> data.sort(Comparator.comparingLong(Mail::getSendTime).reversed());
            case OLDEST -> data.sort(Comparator.comparingLong(Mail::getSendTime));
        }
    }

    @Override
    protected void handleMenuUnCache(String layout) {
        int size =  layout.length();
        /* 当前显示的下标 */
        int appearIndex = ((getPage() - 1) * PAGE_COUNT) - 1;
        for (int i = 0; i < size; i++){
            char c = layout.charAt(i);
            switch (c) {
                case 'A' -> setCheckBoxSlot(i, getBackMenuRunnable() != null);
                case 'D' -> setItemSlot(i, sort.getItemSlot(c));
                case 'F', 'G', 'H' -> setButtonSlot(i, category.getKey() == c);
                case 'I', 'J' -> {
                    int index = (c == 'I') ? ++appearIndex : appearIndex;
                    if (data.size() > index) {
                        setMail(i, c, data.get(index));
                    } else {
                        setItemSlot(i, ItemUtil.AIR);
                    }
                }
                case 'K', 'L' -> setButtonSlot(i, !(getPage() > 1));
                case 'M', 'N' -> setButtonSlot(i, !(getPage() < getMaxPage()));
                case 'E' -> setItemSlot(i, ItemUtil.getItemStackByConfig(getPlugin().getConfigManager(), getConfigPath(),
                        String.format(ICON_DISPLAY, "c"), Map.of("count", String.valueOf(data.size()))));
                default -> setItemSlot(i);
            }
        }
    }

    @Override
    public void topInventoryClickEvent(InventoryClickEvent event, int slot) {
        if (!tryCooldown(slot, getPlugin().getConfigManager()
                .getYmlValue(YML_LANGUAGE,
                        "PlayerMessage.Error.ButtonCooldown"))) {
            return;
        }
        char c = getLayout().charAt(slot);
        asyncPlaySound("Icons." + c + ".Display.Sound");
        switch (c) {
            case 'A' -> backMenu();
            case 'D' -> {
                /* 清除缓存 */
                cleanItemCache();
                this.sort = sort.next();
                sortData();
                goToFirstPage();
                handleMenu(getLayout());
                asyncUpdateTitle();
            }
            /* 分类 */
            case 'F', 'G', 'H' -> {
                if (category.getKey() == c) { return; }
                MailboxCategory oldCategory = category;
                this.category = MailboxCategory.fromKey(c);
                categoryState.categoryChange(category, oldCategory);
                this.data = MAILS.stream().filter(category::filter).collect(Collectors.toList());
                onDataChanged(false);
            }
            case 'K', 'L' -> changePage(- 1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'M', 'N' -> changePage(1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'I', 'J' -> clickMail(slot, c);
        }
    }

    @Override
    public void closeEvent(InventoryCloseEvent e) {
        super.closeEvent(e);
        categoryState.destroy();
    }

    /**
     * 邮箱槽点点击事件
     * @param slot 槽位
     * @param key 当前槽位对应的 Key
     */
    private void clickMail(int slot, Character key) {
        int index = StringUtils
                .countMatches(getLayout().substring(0, slot), "I");
        if (key.equals('J')) {
            index--;
        }
        if (index < 0) return;
        int selectedIndex = (getPage() - 1) * PAGE_COUNT + index;
        Mail mail = data.get(selectedIndex);

        if (!mail.isValid() || mail.getStatus() == MailStatus.EXPIRED
                || mail.getStatus() == MailStatus.READ)
        { return; }


        new MailAttachmentMenu(MailboxParam.of(getPlayer(), mail, createPlaceholders(selectedIndex),
                this::asyncOpenMenu));
    }

    /**
     * 设置邮件槽物品逻辑
     * @param slot 槽位
     * @param key 当前槽位对应的 Key
     * @param mail 邮件信息
     */
    private void setMail(int slot, char key, Mail mail) {
        String mailId = mail.getId();
        if (mailIconCache.containsKey(mailId)) {
            setItemSlot(slot, mailIconCache.get(mailId));
            return;
        }
        Map<String, String> map = new HashMap<>(Map.of(
                "subject", mail.getSubject(),
                "amount", String.valueOf(mail.getAttachment().getAmount()),
                "point", String.valueOf(mail.getAttachment().getPoint()),
                "receiver_name", mail.getReceiverName(),
                "receiver_avatar", MagicPlayerAPI.getInstance().getPlayerAvatar(mail.getReceiverName()),
                "sender_name", mail.getSenderName(),
                "sender_avatar", MagicPlayerAPI.getInstance().getPlayerAvatar(mail.getSenderName()),
                "content", Optional.ofNullable(mail.getContent())
                        .map(content -> String.join("<newline>",
                                LineBreakFormatter.formatWithLineBreaks("<reset><white>" + content, 18)))
                        .orElse("")
        ));
        map.putAll(FormatUtil.formatDateTime(mail.getSendTime(), "send_time_"));
        ItemStack icon = ItemUtil.getItemStackByConfig(
                getPlugin().getConfigManager(),
                getConfigPath(),
                String.format(ICON_DISPLAY, key),
                map
        );
        mailIconCache.put(mailId, icon);
        setItemSlot(slot, icon);
    }

    @Override
    protected LinkedHashMap<String, String> processHandTitle(LinkedHashMap<String, String> title) {
        Map<String, String> map = createPlaceholders(null);
        return title
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry)
                        -> StringUtil.replacePlaceholders(entry.getValue(), map), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }

    protected Map<String, String> createPlaceholders(@Nullable Integer selectedIndex) {
        Map<String, String> map = new HashMap<>();
        /* 变量部分处理 */
        final String SUBJECT = "mail_subject_%s",  // 邮件主题
                HAS = "has_mail_%s", // 存在 Mail
                SELECTED = "selected_mail_%s",
                SUBJECT_FORMAT = "subject_format_%s"; // 邮件主题格式化
        /* 起始坐标 */
        int pageOffset = (getPage() - 1) * PAGE_COUNT;
        for (int i = 0; i < PAGE_COUNT; i++) {
            int index = i + pageOffset, // 邮件的下标
                    mailSort = i + 1; // 邮件的顺序;
            /* 是否存在此下标的邮件 */
            boolean hasMail = index < data.size();
            /* 变量部分处理 */
            String papiSubject = String.format(SUBJECT, mailSort),
                    papiHas = String.format(HAS, mailSort),
                    papiSelected = String.format(SELECTED, mailSort),
                    papiSubjectFormat = String.format(SUBJECT_FORMAT, mailSort);
            /* 是否存在变量 */
            map.put(papiHas, hasMail ? getConfigValue(String.format("CustomPapi.%s.%s",
                    "HasMail_" + mailSort, !data.get(index).isValid() || data.get(index).getStatus() == MailStatus.EXPIRED ? "Expired"
                                    : data.get(index).getStatus() == MailStatus.UNREAD ? "Unread" : "Read"),
                    "", ParseType.STRING) : StringUtils.EMPTY);
            /* 是否选中变量 */
            map.put(papiSelected, getCustomPapiText("SelectedMail_" + mailSort, selectedIndex != null && selectedIndex == index));
            /* 主题格式化变量 */
            map.put(papiSubjectFormat, getCustomPapiText("SubjectFormat_"
                    + mailSort, hasMail));
            /* 主题部分变量 */
            map.put(papiSubject, hasMail ? data.get(index).getSubject() : StringUtils.EMPTY);
        }
        /* 邮件数量 */
        map.put("mail_count", countPapi);
        /* 邮件气泡区域 */
        map.put("mail_count_bubble", countBubblePapi);
        /* 全部分类 */
        map.put("category_all", MagicLib.getInstance().getOffsetManager().format(categoryState
                .getOffset().first(), StringUtils.EMPTY) +
                getCustomPapiText("CategoryAll", category == MailboxCategory.ALL));
        /* 未读分类 */
        map.put("category_unread", MagicLib.getInstance().getOffsetManager().format(categoryState
                .getOffset().second(), StringUtils.EMPTY) +
                getCustomPapiText("CategoryUnread", category == MailboxCategory.UNREAD));
        /* 已读分类 */
        map.put("category_read", MagicLib.getInstance().getOffsetManager().format(categoryState
                .getOffset().third(), StringUtils.EMPTY) +
                getCustomPapiText("CategoryRead", category == MailboxCategory.READ));
        /* 已过期分类 */
        map.put("category_expired", MagicLib.getInstance().getOffsetManager().format(categoryState
                .getOffset().fourth(), StringUtils.EMPTY) +
                getCustomPapiText("CategoryExpired", category == MailboxCategory.EXPIRED));
        /* 排序 - 最新 */
        map.put("sort_newest", getCustomPapiText("SortNewest", sort == MailBoxSort.NEWEST));
        /* 排序 - 旧的 */
        map.put("sort_oldest", getCustomPapiText("SortOldest", sort == MailBoxSort.OLDEST));
        return map;
    }

    /**
     * 计算邮箱邮件数量气泡框
     * @return 邮件数量气泡框
     */
    private String calculateCountBubble() {
        String left = getPlugin().getConfigManager()
                        .getYmlValue(getConfigPath(), "Setting.Count.Bubble.Left"),
                middle = getPlugin().getConfigManager()
                        .getYmlValue(getConfigPath(), "Setting.Count.Bubble.Middle"),
                right = getPlugin().getConfigManager()
                        .getYmlValue(getConfigPath(), "Setting.Count.Bubble.Right");
        int width = MagicLib.getInstance().getAdvanceManager().getAdvance(countPapi),
                middleWidth = MagicLib.getInstance().getAdvanceManager().getAdvance(middle);
        if (middleWidth == 0 || middleWidth > width) {
            return left + middle + right;
        }
        StringBuilder builder = new StringBuilder(left);
        builder.append(middle);
        int nowWidth = middleWidth;
        while (nowWidth < width) {
            builder.append(middle);
            nowWidth += middleWidth;
        }
        builder.append(right);
        return builder.toString();
    }
}

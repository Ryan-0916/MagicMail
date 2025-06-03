package com.magicrealms.magicmail.core.menu;
import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.holder.PageMenuHolder;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.mail.AttachmentItem;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import com.magicrealms.magicmail.core.menu.enums.AttachmentReceiveMethod;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.magicrealms.magicmail.common.MagicMailConstant.YML_LANGUAGE;
import static com.magicrealms.magicmail.common.MagicMailConstant.YML_MAIL_ATTACHMENT_MENU;

/**
 * @author Ryan-0916
 * @Desc 邮件附件菜单
 * @date 2025-05-31
 */
public class MailAttachmentMenu extends PageMenuHolder {
    /* Menu 按钮相关属性 Key */
    String ICON_DISPLAY = "Icons.%s.Display";
    /* 邮件信息 */
    private final Mail DATA;
    /* 待领取物品集 */
    private final List<AttachmentItem> PENDING_RECEIPT_ITEMS;
    /* 父菜单 PaPi */
    private final Map<String, String> PARENT_PAPI;
    /* 每页显示物品数量 */
    private final int PAGE_COUNT;
    /* 选中领取的物品 */
    private final Map<String, ItemStack> SELECTED_ITEMS_INDEX = new HashMap<>();
    /* 领取方式是否为 ALL */
    private final boolean IS_RECEIVE_ALL;
    /* 选择提示 */
    private String selectPrompt = StringUtils.EMPTY;

    public MailAttachmentMenu(MailboxParam param) {
        super(BukkitMagicMail.getInstance(),
                param.player(), YML_MAIL_ATTACHMENT_MENU,
                "#A#####B##CCCCCCC##CCCCCCC##CCCCCCC##D#####E#",
                param.backRunnable());
        this.DATA = param.mail();
        this.PARENT_PAPI = param.titlePapi();
        this.PAGE_COUNT = StringUtils.countMatches(getLayout(), "C");
        this.PENDING_RECEIPT_ITEMS = DATA.getAttachment().getPendingReceiptItems();
        /* 附件领取方式 */
        AttachmentReceiveMethod RECEIVE_METHOD = ItemUtil.canFitIntoInventory(getPlayer(),
                PENDING_RECEIPT_ITEMS.stream().map(AttachmentItem::getItem).toList()) ?
                AttachmentReceiveMethod.ALL : AttachmentReceiveMethod.PARTIAL;
        this.IS_RECEIVE_ALL = RECEIVE_METHOD == AttachmentReceiveMethod.ALL;
        if (IS_RECEIVE_ALL) {
            /* 如果领取方式是全部领取的话，将附件物品ID，与物品存入选中 MAP 中 */
            PENDING_RECEIPT_ITEMS.forEach(e -> SELECTED_ITEMS_INDEX.put(e.getId(), e.getItem()));
        }
        setSelectPrompt("UnSelected");
        setMaxPage(PAGE_COUNT <= 0 || PENDING_RECEIPT_ITEMS.isEmpty() ? 1 :
                PENDING_RECEIPT_ITEMS.size() % PAGE_COUNT == 0 ?
                        PENDING_RECEIPT_ITEMS.size() / PAGE_COUNT : PENDING_RECEIPT_ITEMS
                        .size() / PAGE_COUNT + 1);
        asyncOpenMenu();
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
                case 'B' -> setButtonSlot(i, !IS_RECEIVE_ALL && SELECTED_ITEMS_INDEX.isEmpty());
                case 'C' -> {
                    if (PENDING_RECEIPT_ITEMS.size() > ++appearIndex) {
                        setItemSlot(i, PENDING_RECEIPT_ITEMS.get(appearIndex).getItem());
                    } else {
                        setItemSlot(i, ItemUtil.AIR);
                    }
                }
                case 'D' -> setButtonSlot(i, !(getPage() > 1));
                case 'E' -> setButtonSlot(i, !(getPage() < getMaxPage()));
                case 'F', 'G' -> setItemSlot(i, ItemUtil.getItemStackByConfig(
                        getPlugin().getConfigManager(),
                        getConfigPath(),
                        String.format(ICON_DISPLAY, c),
                        Map.of("amount", String.valueOf(DATA.getAttachment().getAmount()),
                                "point", String.valueOf(DATA.getAttachment().getPoint()))
                ));
                default -> setItemSlot(i);
            }
        }
    }

    @Override
    protected LinkedHashMap<String, String> processHandTitle(LinkedHashMap<String, String> title) {
        Map<String, String> map = createPlaceholders();
        map.putAll(PARENT_PAPI);
        return title
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry)
                        -> StringUtil.replacePlaceholders(entry.getValue(), map), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }

    protected Map<String, String> createPlaceholders() {
        final boolean hasSelectedItems = !SELECTED_ITEMS_INDEX.isEmpty();
        Map<String, String> map = new HashMap<>();
        map.put("receive_button", getCustomPapiText("ReceiveButton", IS_RECEIVE_ALL || hasSelectedItems));
        map.put("select_prompt", selectPrompt);
        /* 领取全部 */
        if (IS_RECEIVE_ALL) {
            int rowCount = getLayout().length() / 9;
            IntStream.range(0, rowCount)
                    .forEach(i -> map.put("selected_item_row_" + i, StringUtils.EMPTY));
            return map;
        }
        /* 选中领取，动态计算每行显示的背景框 */
        int pageOffset = (getPage() - 1) * PAGE_COUNT;
        Map<Integer, StringBuilder> rowPapi = new HashMap<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            int index = i + pageOffset;
            int row = findRowOfItemIndex(i);
            rowPapi.computeIfAbsent(row, k -> new StringBuilder())
                    .append(getCustomPapiText("SelectedItemRow_" + row,
                            PENDING_RECEIPT_ITEMS.size() > index && SELECTED_ITEMS_INDEX.containsKey(PENDING_RECEIPT_ITEMS
                                    .get(index).getId())));
        }
        rowPapi.forEach((row, builder) ->
                map.put("selected_item_row_" + row, builder.toString()));
        return map;
    }

    private int findRowOfItemIndex(int itemIndex) {
        if (itemIndex < 0) return -1;
        AtomicInteger counter = new AtomicInteger();
        return IntStream.range(0, getLayout().length())
                .filter(i -> getLayout().charAt(i) == 'C')
                .filter(i -> counter.getAndIncrement() == itemIndex)
                .map(i -> (i / 9) + 1)
                .findFirst()
                .orElse(-1);
    }

    @Override
    public void topInventoryClickEvent(InventoryClickEvent event, int slot) {
        if (!tryCooldown(slot, getPlugin()
                .getConfigManager()
                .getYmlValue(YML_LANGUAGE,"PlayerMessage.Error.ButtonCooldown"))) {
            return;
        }
        char c = getLayout().charAt(slot);
        asyncPlaySound("Icons." + c + ".Display.Sound");
        switch (c) {
            case 'A'-> backMenu();
            case 'C' -> clickItems(slot);
            case 'D' -> changePage(- 1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'E' -> changePage(1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'B' -> receive();
        }
    }

    private void receive() {
        if (!IS_RECEIVE_ALL && SELECTED_ITEMS_INDEX.isEmpty()) {
            return;
        }
        asyncCloseMenu();
        BukkitMagicMail.getInstance().getMailRepository().receiveMail(getPlayer(),
                DATA.getId(),
                SELECTED_ITEMS_INDEX.keySet()
                        .toArray(new String[0]));
    }

    private void clickItems(int slot) {
        if (IS_RECEIVE_ALL) {
            return;
        }
        int index = StringUtils.countMatches(getLayout().substring(0, slot), "C");
        if (index < 0) return;
        int selectedIndex = (getPage() - 1) * PAGE_COUNT + index;
        AttachmentItem item = PENDING_RECEIPT_ITEMS.get(selectedIndex);
        boolean wasRemoved = SELECTED_ITEMS_INDEX.remove(item.getId()) != null;
        if (!wasRemoved) {
            ItemStack newItem = item.getItem();
            List<ItemStack> combinedItems = Stream.concat(
                    SELECTED_ITEMS_INDEX.values().stream(),
                    Stream.of(newItem)
            ).toList();
            if (!ItemUtil.canFitIntoInventory(getPlayer(), combinedItems)) {
                setSelectPrompt("Full");
                asyncUpdateTitle();
                return;
            }
            SELECTED_ITEMS_INDEX.put(item.getId(), newItem);
        }
        setSelectPrompt(!SELECTED_ITEMS_INDEX.isEmpty() ? "Selected" : "UnSelected", SELECTED_ITEMS_INDEX.size());
        asyncUpdateTitle();
    }

    private void setSelectPrompt(String type, Integer... placeholders) {
        if (IS_RECEIVE_ALL) {
            selectPrompt = StringUtils.EMPTY;
            return;
        }
        String path = String.format("CustomPapi.%s.%s", "SelectPrompt", type);
        String message = getConfigValue(path, StringUtils.EMPTY, ParseType.STRING);
        if ("Selected".equals(type)) {
            Objects.requireNonNull(placeholders);
            message = StringUtil.replacePlaceholder(message,
                    "selected_count", String.valueOf(placeholders[0]));
        }
        selectPrompt = message;
    }

}

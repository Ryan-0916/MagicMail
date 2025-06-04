package com.magicrealms.magicmail.api.util;

import com.magicrealms.magiclib.bukkit.manage.ConfigManager;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.MagicMail;
import com.magicrealms.magicmail.api.MagicMailAPI;
import com.magicrealms.magicmail.api.mail.AttachmentItem;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.api.mail.MailAttachment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.magicrealms.magicmail.common.MagicMailConstant.YML_CONFIG;

/**
 * @author Ryan-0916
 * @Desc 玩家背包工具类
 * @date 2025-06-02
 */
public final class PlayerInventoryUtil {

    /**
     * 发送物品返还邮箱给玩家
     * @param player 玩家
     * @param items 返还物品列表
     * @param templateKey YML 模板 KEY
     */
    private static void sendReturnMail(Player player, List<ItemStack> items, String templateKey) {
        ConfigManager configManager = MagicMail.getInstance().getConfigManager();
        Mail mail = Mail.builder(player)
                .subject(configManager.getYmlValue(YML_CONFIG, templateKey + ".Subject"))
                .content(configManager.getYmlValue(YML_CONFIG, templateKey + ".Content"))
                .attachment(MailAttachment.builder()
                        .items(items.stream().map(AttachmentItem::new).toList())
                        .build())
                .build();
        MagicMailAPI.getInstance().sendMail(mail);
    }

    /**
     * 给予玩家物品至玩家背包
     * 该方法如若玩家异常导致的无法获取到该物品会通过邮件返还
     * 情况1：调用该方法时玩家处于离线状态，物品将返还至玩家邮箱
     * 情况2：玩家背包无法容纳下这些物品，物品将返还之玩家邮箱
     * @param player 玩家
     * @param items 给予物品列表
     */
    public static void givePlayerItems(Player player, List<ItemStack> items) {
        if (items.isEmpty()) {
            return;
        }
        if (!player.isOnline()) {
            sendReturnMail(player, items, "MailTemplate.OfflineItemReturn");
            return;
        }
        /* 玩家在线，尝试添加到背包 */
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> remainingItems = new ArrayList<>();
        items.forEach(item -> {
            if (item.getAmount() == item.getMaxStackSize()) {
                tryAddToInventory(inventory, item, remainingItems);
            } else {
                trySimilarOrAddToInventory(inventory, item, remainingItems);
            }
        });
        /* 如果有剩余的物品，尝试将玩家的背包融合 */
        if (!remainingItems.isEmpty()) {
            inventory.setStorageContents(ItemUtil.mergeSimilarItemStacks(Arrays.asList(inventory
                    .getStorageContents())).toArray(ItemStack[]::new));
            /* 将剩余的物品放置 */
            remainingItems.removeIf(item -> {
                int slot = inventory.firstEmpty();
                if (slot != -1) {
                    inventory.setItem(slot, item);
                    return true;
                }
                return false;
            });
            /* 如果还有剩余物品，发送邮件 */
            if (!remainingItems.isEmpty()) {
                sendReturnMail(player, remainingItems, "MailTemplate.FullInventoryItemReturn");
            }
        }
    }

    /**
     * 尝试将物品塞入玩家背包
     * @param inventory 玩家背包
     * @param item 物品
     * @param remainingItems 如果塞不下将会存入这个列表中
     */
    private static void tryAddToInventory(PlayerInventory inventory, ItemStack item, List<ItemStack> remainingItems) {
        int emptySlot = inventory.firstEmpty();
        if (emptySlot != -1) {
            inventory.setItem(emptySlot, item);
            return;
        }
        remainingItems.add(item);
    }

    /**
     * 尝试将物品与玩家背包内的相同物品合并，或者塞入玩家背包
     * 如果物品可以与玩家背包内物品合并，那么将与玩家背包内物品合并
     * 否则将直接塞入玩家背包
     * @param inventory 玩家背包
     * @param item 物品
     * @param remainingItems 如果塞不下将会存入这个列表中
     */
    private static void trySimilarOrAddToInventory(PlayerInventory inventory, ItemStack item, List<ItemStack> remainingItems) {
        int maxItemSize = item.getMaxStackSize();
        if (item.getAmount() != maxItemSize) {
            inventory.all(item).forEach((index, inventoryItem) ->
                    ItemUtil.similarItem(inventoryItem, item));
        }
        /* 如果物品已经被堆叠到背包中 */
        if (item.getAmount() <= 0) return;
        tryAddToInventory(inventory, item, remainingItems);
    }


}

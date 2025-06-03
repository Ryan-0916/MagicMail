package com.magicrealms.magicmail.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.Command;
import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.mail.AttachmentItem;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.api.mail.MailAttachment;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import com.magicrealms.magicmail.core.menu.MailboxMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Ryan-0916
 * @Desc 邮箱部分命令
 * @date 2025-05-28
 */
@SuppressWarnings("unused")
@CommandListener
public class MailController {

    @Command(text = "^\\s?$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.mail",
            label = "^mail$")
    public void mail(Player sender, String[] args){
        new MailboxMenu(sender);
    }

    @Command(text = "^test\\s\\S+\\s\\S+$", permissionType = PermissionType.PLAYER)
    public void test(Player sender, String[] args){
        List<AttachmentItem> items = new ArrayList<>();
        for (ItemStack itemStack : sender.getInventory()) {
            if (ItemUtil.isNotAirOrNull(itemStack)) {
                items.add(new AttachmentItem(itemStack));
            }
        }
        BukkitMagicMail.getInstance().getMailRepository().sendMail(
                Mail.builder(sender)
                        .subject(args[1])
                        .attachment(MailAttachment.builder()
                                .point(20)
                                .amount(BigDecimal.valueOf(1000))
                                .items(items)
                                .build())
                        .content(args[2])
                        .build()
        );

    }

}

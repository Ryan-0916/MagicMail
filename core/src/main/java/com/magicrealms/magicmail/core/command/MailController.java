package com.magicrealms.magicmail.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.Command;
import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import com.magicrealms.magicmail.core.menu.MailboxMenu;
import org.bukkit.entity.Player;


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

    @Command(text = "^test\\s\\S+$", permissionType = PermissionType.PLAYER)
    public void test(Player sender, String[] args){
        BukkitMagicMail.getInstance().getMailRepository().sendMail(
                Mail.builder(sender)
                        .subject(args[1])
                        .content("刘一鸣大傻逼")
                        .build()
        );

    }

}

package com.magicrealms.magicmail.core.menu;

import com.magicrealms.magicmail.api.mail.Mail;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author Ryan-0916
 * @Desc 邮箱子菜单参数
 * @date 2025-05-31
 */
public record MailboxParam(Player player,
                           Mail mail,
                           Map<String, String> titlePapi,
                           Runnable backRunnable) {

    public static MailboxParam of(Player player,
                                  Mail mail,
                                  Map<String, String> titlePapi,
                                  Runnable backRunnable) {
        return new MailboxParam(player, mail, titlePapi, backRunnable);
    }
}
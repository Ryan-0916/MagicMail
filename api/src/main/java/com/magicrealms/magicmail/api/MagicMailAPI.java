package com.magicrealms.magicmail.api;

import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.api.mail.repository.MailRepository;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Ryan-0916
 * @Desc MagicMail API
 * @date 2025-05-17
 */
@SuppressWarnings("unused")
public record MagicMailAPI(MagicMail plugin) {

    private static MagicMailAPI instance;

    public MagicMailAPI(MagicMail plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static MagicMailAPI getInstance() {
        if (instance == null) {
            throw new RuntimeException("MagicMail API 未被初始化");
        }
        return instance;
    }

    /**
     * query player all received mail
     * {@link MailRepository#queryMailBox(Player)}
     */
    public List<Mail> queryMailBox(Player player) {
        return plugin.getMailRepository().queryMailBox(player);
    }

    /**
     * send mail to player mailbox
     * {@link MailRepository#sendMail(Mail)}
     */
    public void sendMail(Mail mail) {
        plugin.getMailRepository().sendMail(mail);
    }
}

package com.magicrealms.magicmail.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.Command;
import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import com.magicrealms.magiclib.core.dispatcher.MessageDispatcher;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import org.bukkit.command.CommandSender;

import java.util.Locale;

import static com.magicrealms.magicmail.common.MagicMailConstant.*;

/**
 * @author Ryan-0916
 * @Desc 核心部分命令
 * @date 2025-05-17
 */
@CommandListener
@SuppressWarnings("unused")
public class CoreController {

    private void setupCommon() {
        BukkitMagicMail.getInstance().setupMailRepository();
    }

    @Command(text = "^Reload$",
            permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.reload", label = "^magicMail$")
    public void reload(CommandSender sender, String[] args){
        BukkitMagicMail.getInstance().getConfigManager().reloadConfig(YML_REDIS, YML_MONGODB);
        setupCommon();
        MessageDispatcher.getInstance().sendMessage(BukkitMagicMail.getInstance(), sender,
                        BukkitMagicMail.getInstance().getConfigManager()
                                .getYmlValue(YML_LANGUAGE,
                        "PlayerMessage.Success.ReloadFile"));
    }

    @Command(text = "^Reload\\sAll$",
            permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.reload", label = "^magicMail$")
    public void reloadAll(CommandSender sender, String[] args){
        BukkitMagicMail.getInstance().getConfigManager().reloadAllConfig();
        /* 重置 Redis 部分 */
        BukkitMagicMail.getInstance().setupRedisStore();
        /* 重置 MongoDB 部分 */
        BukkitMagicMail.getInstance().setupMongoDB();
        setupCommon();
        MessageDispatcher.getInstance().sendMessage(BukkitMagicMail.getInstance(), sender,
                        BukkitMagicMail.getInstance().getConfigManager()
                                .getYmlValue(YML_LANGUAGE,
                                        "PlayerMessage.Success.ReloadFile"));
    }

    @Command(text = "^Reload\\s(?!all\\b)\\S+$", permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.reload", label = "^magicMail$")
    public void reloadBy(CommandSender sender, String[] args){
        BukkitMagicMail.getInstance().getConfigManager()
                .reloadConfig(args[1], e -> {
            if (!e) {
                MessageDispatcher.getInstance().sendMessage(BukkitMagicMail.getInstance(), sender,
                        BukkitMagicMail.getInstance().getConfigManager().getYmlValue(YML_LANGUAGE,
                                "PlayerMessage.Error.ReloadFile"));
                return;
            }
            switch (args[1].toLowerCase(Locale.ROOT)) {
                case "config" -> BukkitMagicMail.getInstance().setupMailRepository();
                case "redis" ->  {
                    BukkitMagicMail.getInstance().setupRedisStore();
                    BukkitMagicMail.getInstance().setupMailRepository();
                }
                case "mongodb" -> {
                    BukkitMagicMail.getInstance().setupMongoDB();
                    BukkitMagicMail.getInstance().setupMailRepository();
                }
            }
            MessageDispatcher.getInstance().sendMessage(BukkitMagicMail.getInstance(), sender,
                    BukkitMagicMail.getInstance().getConfigManager()
                            .getYmlValue(YML_LANGUAGE,
                            "PlayerMessage.Success.ReloadFile"));
        });
    }
}

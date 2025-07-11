package com.magicrealms.magicmail.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.annotations.TabComplete;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Ryan-0916
 * @Desc 核心部分命令补全
 * @date 2025-05-17
 */
@CommandListener
@SuppressWarnings("unused")
public class CoreTabController {

    private static final Supplier<Stream<String>> fileNames
            = () -> Stream.of("all", "config", "language", "redis", "mongodb", "mailBoxMenu", "mailAttachmentMenu");

    @TabComplete(text = "^\\s?$", permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.reload", label = "^magicMail$")
    public List<String> first(CommandSender sender, String[] args) {
        return Stream.of("reload")
                .toList();
    }

    @TabComplete(text = "^\\S+$", permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.reload", label = "^magicMail$")
    public List<String> firstTab(CommandSender sender, String[] args) {
        return Stream.of("reload")
                .filter(e ->
                        StringUtils.startsWithIgnoreCase(e, args[0]))
                .toList();
    }

    @TabComplete(text = "^Reload\\s$", permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.reload", label = "^magicMail$")
    public List<String> reload(CommandSender sender, String[] args) {
        return fileNames.get()
                .toList();
    }

    @TabComplete(text = "^Reload\\s\\S+$", permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmail.all||magic.command.magicmail.reload", label = "^magicMail$")
    public List<String> reloadTab(CommandSender sender, String[] args) {
        return fileNames.get().filter(e ->
                StringUtils.startsWithIgnoreCase(e, args[1]))
                .toList();
    }
}

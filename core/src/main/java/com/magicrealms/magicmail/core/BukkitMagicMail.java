package com.magicrealms.magicmail.core;

import com.magicrealms.magiclib.bukkit.manage.BungeeMessageManager;
import com.magicrealms.magiclib.bukkit.manage.CommandManager;
import com.magicrealms.magiclib.bukkit.manage.ConfigManager;
import com.magicrealms.magiclib.bukkit.manage.PacketManager;
import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.store.MongoDBStore;
import com.magicrealms.magiclib.common.store.RedisStore;
import com.magicrealms.magiclib.core.dispatcher.MessageDispatcher;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.MagicMail;
import com.magicrealms.magicmail.api.MagicMailAPI;
import com.magicrealms.magicmail.api.mail.repository.MailRepository;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Optional;

import static com.magicrealms.magicmail.common.MagicMailConstant.*;

/**
 * @author Ryan-0916
 * @Desc MagicMail Plugin
 * @date 2025-05-17
 */
public class BukkitMagicMail extends MagicMail {

    @Getter
    private static BukkitMagicMail instance;

    @Getter
    private BungeeMessageManager bungeeMessageManager;

    public BukkitMagicMail() {
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        dependenciesCheck(() -> {
            /* 加载配置文件 */
            loadConfig(configManager);
            /* 注册指令 */
            registerCommand(commandManager);
            /* 注册数据包监听器 */
            registerPacketListener(packetManager);
            /* 初始化 Redis */
            setupRedisStore();
            /* 初始化 MongoDB */
            setupMongoDB();
            /* 初始化邮件持久层 */
            setupMailRepository();
            /* 初始化 API */
            setupAPI();
        });
    }

    private void setupAPI() {
        this.api = new MagicMailAPI(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        unsubscribe();
    }

    public void setupRedisStore() {
        String host = getConfigManager().getYmlValue(YML_REDIS, "DataSource.Host"), password = getConfigManager().getYmlValue(YML_REDIS, "DataSource.Password");
        int port = getConfigManager().getYmlValue(YML_REDIS, "DataSource.Port", 6379, ParseType.INTEGER);
        boolean redisPasswordModel = getConfigManager().getYmlValue(YML_REDIS, "DataSource.PasswordModel", false, ParseType.BOOLEAN);
        /* ItemUtil GSON 的目的是为了让 Gson 兼容 ItemStack 的转换 */
        this.redisStore = new RedisStore(ItemUtil.GSON, host, port, redisPasswordModel ? password : null);
        this.unsubscribe();
        this.bungeeMessageManager = new BungeeMessageManager.Builder().channel(BUNGEE_CHANNEL)
                .plugin(this)
                .host(host)
                .port(port)
                .passwordModel(redisPasswordModel)
                .password(password)
                .messageListener(e -> {
                    switch (e.getType()) {
                        case SERVER_MESSAGE
                                -> MessageDispatcher.getInstance().sendBroadcast(this, e.getMessage());
                        case PLAYER_MESSAGE
                                -> Optional.ofNullable(Bukkit.getPlayerExact(e.getRecipientName())).ifPresent(player -> MessageDispatcher.getInstance().sendMessage(this, player, e.getMessage()));
                    }
                }).build();
    }

    public void setupMongoDB() {
        String host = getConfigManager().getYmlValue(YML_MONGODB, "DataSource.Host")
                , database = getConfigManager().getYmlValue(YML_MONGODB, "DataSource.Database");
        int port = getConfigManager().getYmlValue(YML_MONGODB, "DataSource.Port", 27017, ParseType.INTEGER);
        this.mongoDBStore = new MongoDBStore(host, port, database);
    }

    public void setupMailRepository() {
        this.mailRepository = new MailRepository(mongoDBStore, MAGIC_MAIL_TABLE_NAME, redisStore);
    }

    private void unsubscribe() {
        Optional.ofNullable(bungeeMessageManager)
                .ifPresent(BungeeMessageManager::unsubscribe);
    }

    @Override
    protected void loadConfig(ConfigManager configManager) {
        configManager.loadConfig(YML_CONFIG, YML_LANGUAGE, YML_MONGODB, YML_REDIS,
                YML_MAILBOX_MENU, YML_MAIL_ATTACHMENT_MENU);
    }

    @Override
    protected void registerCommand(CommandManager commandManager) {
        commandManager.registerCommand(PLUGIN_NAME, e -> {
            switch (e.cause()) {
                case NOT_PLAYER -> MessageDispatcher.getInstance().
                        sendMessage(this, e.sender(), getConfigManager().getYmlValue(YML_LANGUAGE,
                                "ConsoleMessage.Error.NotPlayer"));
                case NOT_CONSOLE -> MessageDispatcher.getInstance().
                        sendMessage(this, e.sender(), getConfigManager().getYmlValue(YML_LANGUAGE,
                                "PlayerMessage.Error.NotConsole"));
                case UN_KNOWN_COMMAND -> MessageDispatcher.getInstance().
                        sendMessage(this, e.sender(), getConfigManager().getYmlValue(YML_LANGUAGE,
                                "PlayerMessage.Error.UnknownCommand"));
                case PERMISSION_DENIED -> MessageDispatcher.getInstance().
                        sendMessage(this, e.sender(), getConfigManager().getYmlValue(YML_LANGUAGE,
                                "PlayerMessage.Error.PermissionDenied"));
            }
        });
    }

    @Override
    protected void registerPacketListener(PacketManager packetManager) { }

}

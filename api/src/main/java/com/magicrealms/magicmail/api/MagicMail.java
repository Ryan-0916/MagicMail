package com.magicrealms.magicmail.api;

import com.magicrealms.magiclib.bukkit.MagicRealmsPlugin;
import com.magicrealms.magiclib.common.store.MongoDBStore;
import com.magicrealms.magiclib.common.store.RedisStore;
import com.magicrealms.magicmail.api.mail.repository.MailRepository;
import lombok.Getter;

/**
 * @author Ryan-0916
 * @Desc Abstract MagicMail Plugin
 * @date 2025-05-17
 */
public abstract class MagicMail extends MagicRealmsPlugin {

    protected MagicMailAPI api;

    @Getter
    private static MagicMail instance;

    @Getter
    protected RedisStore redisStore;

    @Getter
    protected MongoDBStore mongoDBStore;

    @Getter
    protected MailRepository mailRepository;

    public MagicMail() {
        instance = this;
    }

}

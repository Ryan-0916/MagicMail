package com.magicrealms.magicmail.common;

/**
 * @author Ryan-0916
 * @Desc 常量
 * @date 2025-05-17
 */
public final class MagicMailConstant {
    /* 插件名称 */
    public static final String PLUGIN_NAME = "MagicMail";

    /** 配置文件部分常量 */
    public static final String YML_CONFIG = "config";
    public static final String YML_REDIS = "redis";
    public static final String YML_LANGUAGE = "language";
    public static final String YML_MONGODB = "mongodb";
    public static final String YML_MAILBOX_MENU = "menu/mailboxMenu";
    public static final String YML_MAIL_ATTACHMENT_MENU = "menu/mailAttachmentMenu";

    /** Redis 相关 key */
    /* 跨服通讯频道 */
    public static final String BUNGEE_CHANNEL = "BUNGEE_CHANNEL_MAGIC_MAIL";
    /* 玩家的邮箱 */
    public static final String MAGIC_MAIL_RECEIVED_MAILS = "MAGIC_MAIL_RECEIVED_MAILS_%s";
    /* 领取邮箱 Lock */
    public static final String MAGIC_MAIL_RECEIVE_LOCK = "MAGIC_MAIL_RECEIVE_LOCK_%s";

    /** MongoDB部分常量 */
    /* MongoDB 玩家信息表 */
    public static final String MAGIC_MAIL_TABLE_NAME = "magic_mail";
}

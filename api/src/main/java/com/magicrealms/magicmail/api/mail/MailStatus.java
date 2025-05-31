package com.magicrealms.magicmail.api.mail;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author Ryan-0916
 * @Desc 邮件状态
 * @date 2025-05-17
 */
@Getter
public enum MailStatus {
    UNREAD(0, "未读"),
    READ(1, "已读"),
    REMOVED(2, "已删除");

    private final int value;

    private final String desc;

    MailStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MailStatus fromCode(Integer code) {
        if (code == null) { return UNREAD; }
        return Arrays.stream(MailStatus.values()).filter(e -> e.getValue() == code)
                .findFirst()
                .orElse(UNREAD);
    }
}

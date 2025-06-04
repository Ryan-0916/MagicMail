package com.magicrealms.magicmail.core.menu.enums;

import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.api.mail.MailStatus;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author Ryan-0916
 * @Desc 邮箱分类方式
 * @date 2025-05-28
 */
@Getter
public enum MailboxCategory {
    ALL("全部", 'F'),
    UNREAD("未读", 'G'),
    READ("已读", 'H'),
    EXPIRED("已过期", 'O');


    private final String displayName;
    private final char key;


    MailboxCategory(String displayName, char key) {
        this.displayName = displayName;
        this.key = key;
    }

    public static MailboxCategory fromKey(Character code) {
        if (code == null) { return READ; }
        return Arrays.stream(MailboxCategory.values()).filter(e -> e.getKey() == code)
                .findFirst()
                .orElse(READ);
    }

    public boolean filter(Mail mail) {
        return this == ALL || (this == EXPIRED && (!mail.isValid() || mail.getStatus() == MailStatus.EXPIRED))
                || (this == READ && mail.getStatus() == MailStatus.READ)
                || (this == UNREAD && mail.getStatus() == MailStatus.UNREAD);
    }
}

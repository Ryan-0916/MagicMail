package com.magicrealms.magicmail.api.mail.adapter;

import com.magicrealms.magiclib.common.adapt.FieldAdapter;
import com.magicrealms.magicmail.api.mail.MailStatus;

import java.util.Optional;

/**
 * @author Ryan-0916
 * @Desc 邮件状态 转换器
 * @date 2025-05-17
 */
public class MailStatusFieldAdapter extends FieldAdapter<MailStatus, Integer> {

    @Override
    public Integer write(MailStatus writer) {
        return writer == null ? 0 : writer.getValue();
    }

    @Override
    public MailStatus read(Integer reader) {
        return Optional.ofNullable(reader)
                .map(MailStatus::fromCode)
                .orElse(MailStatus.UNREAD);
    }

}

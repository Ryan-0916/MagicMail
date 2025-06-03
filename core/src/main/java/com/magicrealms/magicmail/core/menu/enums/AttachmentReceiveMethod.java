package com.magicrealms.magicmail.core.menu.enums;

import lombok.Getter;

/**
 * @author Ryan-0916
 * @Desc 附件领取类型
 * @date 2025-06-01
 */
@Getter
public enum AttachmentReceiveMethod {

    ALL("全部领取"),
    PARTIAL("部分领取");

    private final String name;

    AttachmentReceiveMethod(String name) {
        this.name = name;
    }

}

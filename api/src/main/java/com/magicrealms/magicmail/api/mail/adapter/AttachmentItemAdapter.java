package com.magicrealms.magicmail.api.mail.adapter;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;
import com.magicrealms.magiclib.common.adapt.FieldAdapter;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.mail.AttachmentItem;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ryan-0916
 * @Desc 附件物品转换器
 * @date 2025-05-17
 */
public class AttachmentItemAdapter extends FieldAdapter<List<AttachmentItem>, String> {

    private static final Type TYPE
            = new TypeToken<List<AttachmentItem>>() {}.getType();

    @Override
    public String write(List<AttachmentItem> items) {
        return ItemUtil.GSON.toJson(items != null ? items.stream()
                .filter(Objects::nonNull)
                .toList() : Collections.emptyList());
    }

    @Override
    public List<AttachmentItem> read(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            return ItemUtil.GSON.fromJson(json, TYPE);
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }
}

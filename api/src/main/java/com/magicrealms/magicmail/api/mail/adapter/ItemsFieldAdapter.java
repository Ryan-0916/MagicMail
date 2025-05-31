package com.magicrealms.magicmail.api.mail.adapter;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;
import com.magicrealms.magiclib.common.adapt.FieldAdapter;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 物品集合 转换器
 * @date 2025-05-17
 */
public class ItemsFieldAdapter extends FieldAdapter<List<ItemStack>, String> {

    private static final Type ITEM_STACK_LIST_TYPE
            = new TypeToken<List<ItemStack>>() {}.getType();

    @Override
    public String write(List<ItemStack> items) {
        return ItemUtil.GSON.toJson(items != null ? items : Collections.emptyList());
    }

    @Override
    public List<ItemStack> read(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            return ItemUtil.GSON.fromJson(json, ITEM_STACK_LIST_TYPE);
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }
}

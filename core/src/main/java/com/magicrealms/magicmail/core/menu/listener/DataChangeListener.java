package com.magicrealms.magicmail.core.menu.listener;

/**
 * @author Ryan-0916
 * @Desc 数据变化监听器接口
 * 采用观察者模式观察数据的变更
 * @date 2025-05-30
 */
public interface DataChangeListener {
    void onDataChanged(boolean isInit);
}

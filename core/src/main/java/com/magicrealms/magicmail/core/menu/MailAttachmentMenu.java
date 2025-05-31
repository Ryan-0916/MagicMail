package com.magicrealms.magicmail.core.menu;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.holder.PageMenuHolder;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.core.BukkitMagicMail;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.magicrealms.magicmail.common.MagicMailConstant.YML_MAIL_ATTACHMENT_MENU;

/**
 * @author Ryan-0916
 * @Desc 邮件附件菜单
 * @date 2025-05-31
 */
public class MailAttachmentMenu extends PageMenuHolder {

    /* 邮件信息 */
    private final Mail DATA;
    /* 父菜单 PaPi */
    private final Map<String, String> PARENT_PAPI;
    /* 每页显示物品数量 */
    private final int PAGE_COUNT;
    public MailAttachmentMenu(MailboxParam param) {
        super(BukkitMagicMail.getInstance(),
                param.player(), YML_MAIL_ATTACHMENT_MENU,
                "#A#####B##CCCCCCC##CCCCCCC##CCCCCCC##D#####E#",
                param.backRunnable());
        this.DATA = param.mail();
        this.PARENT_PAPI = param.titlePapi();
        this.PAGE_COUNT = StringUtils.countMatches(getLayout(), "C");
        setMaxPage(PAGE_COUNT <= 0 || DATA.getAttachment().getItems().isEmpty() ? 1 :
                DATA.getAttachment().getItems().size() % PAGE_COUNT == 0 ?
                        DATA.getAttachment().getItems().size() / PAGE_COUNT : DATA.getAttachment().getItems()
                        .size() / PAGE_COUNT + 1);
        asyncOpenMenu();
    }

    @Override
    protected void handleMenuUnCache(String layout) {
        int size =  layout.length();
        /* 当前显示的下标 */
        int appearIndex = ((getPage() - 1) * PAGE_COUNT) - 1;
        for (int i = 0; i < size; i++){
            switch (layout.charAt(i)) {
                case 'A' -> setCheckBoxSlot(i, getBackMenuRunnable() != null);
                case 'C' -> {
                    if (DATA.getAttachment().getItems().size() > ++appearIndex) {
                        setItemSlot(i, DATA.getAttachment().getItems().get(appearIndex));
                    } else {
                        setItemSlot(i, ItemUtil.AIR);
                    }
                }
                case 'E' -> setButtonSlot(i, !(getPage() > 1));
                case 'F' -> setButtonSlot(i, !(getPage() < getMaxPage()));
                default -> setItemSlot(i);
            }
        }
    }

    @Override
    protected LinkedHashMap<String, String> processHandTitle(LinkedHashMap<String, String> title) {
        Map<String, String> map = createPlaceholders();
        map.putAll(PARENT_PAPI);
        return title
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry)
                        -> StringUtil.replacePlaceholders(entry.getValue(), map), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }

    protected Map<String, String> createPlaceholders() {
        return new HashMap<>();
    }

}

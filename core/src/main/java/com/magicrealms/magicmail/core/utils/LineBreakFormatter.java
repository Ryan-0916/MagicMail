package com.magicrealms.magicmail.core.utils;

import com.magicrealms.magiclib.bukkit.message.helper.AdventureHelper;

import java.util.*;

/**
 * @author Ryan-0916
 * @Desc 分行格式化转换器
 * @date 2025-05-31
 */
public final class LineBreakFormatter {

    public static List<String> formatWithLineBreaks(String text, int lineLength) {
        text = AdventureHelper.legacyToMiniMessage(text);
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        Deque<String> activeTags = new ArrayDeque<>();
        int currentPos = 0;
        int textLengthCount = 0;
        while (currentPos < text.length()) {
            if (text.charAt(currentPos) == '<') {
                int tagEnd = text.indexOf('>', currentPos);
                if (tagEnd == -1) {
                    currentLine.append(text.charAt(currentPos));
                    textLengthCount++;
                    currentPos++;
                    continue;
                }
                String tagContent = text.substring(currentPos + 1, tagEnd);
                boolean isClosingTag = tagContent.startsWith("/");
                String tagName = isClosingTag ? tagContent.substring(1) : tagContent;
                String baseTagName = parseBaseTagName(tagName); // 提取基础标签名（如 "font:minecraft:1" -> "font"）

                if (AdventureHelper.getMiniMessage().tags().has(baseTagName)) {
                    String fullTag = "<" + tagContent + ">";
                    currentLine.append(fullTag);
                    if (isClosingTag) {
                        if (!activeTags.isEmpty() && activeTags.peek().equals(tagName)) {
                            activeTags.pop();
                        }
                    } else {
                        activeTags.push(tagName);
                    }
                    currentPos = tagEnd + 1;
                } else {
                    currentLine.append(text.charAt(currentPos));
                    textLengthCount++;
                    currentPos++;
                }
            } else {
                char c = text.charAt(currentPos);
                currentLine.append(c);
                textLengthCount++;
                currentPos++;
                if (textLengthCount >= lineLength) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                    textLengthCount = 0;
                    reopenAllTags(currentLine, activeTags);
                }
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private static String parseBaseTagName(String tagName) {
        int colonPos = tagName.indexOf(':');
        return colonPos == -1 ? tagName : tagName.substring(0, colonPos);
    }

    private static void reopenAllTags(StringBuilder builder, Deque<String> tags) {
        List<String> reversed = new ArrayList<>(tags);
        Collections.reverse(reversed);
        for (String tag : reversed) {
            builder.append("<").append(tag).append(">");
        }
    }
}

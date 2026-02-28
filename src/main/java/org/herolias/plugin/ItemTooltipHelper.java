package org.herolias.plugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemUtility;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for building rich item tooltips with proper color formatting.
 * Based on the EcotaleMarket ItemTooltipBuilder pattern.
 */
public class ItemTooltipHelper {

    private static final String DEFAULT_COLOR = "#c9d2dd";
    private static final String LORE_COLOR = "#a0b0c0";
    private static final String STAT_COLOR = "#c9d2dd";
    private static final String STAT_VALUE_COLOR = "#ffffff";
    private static final String VIRTUAL_SEPARATOR = "__dtt_";

    /**
     * Builds a rich tooltip Message for an item with name, stats, and description.
     */
    @Nonnull
    public static Message buildTooltip(@Nonnull String itemId, int quantity, @Nullable String locale) {
        if (locale == null || locale.isEmpty()) {
            locale = "en-US";
        }

        List<Message> parts = new ArrayList<>();

        String baseId = itemId;
        boolean isVirtual = false;
        if (itemId.contains(VIRTUAL_SEPARATOR)) {
            isVirtual = true;
            baseId = itemId.substring(0, itemId.indexOf(VIRTUAL_SEPARATOR));
        }

        Item item = null;
        try {
            item = (Item) Item.getAssetMap().getAsset(baseId);
        } catch (Exception ignored) {}

        // Item name (translated, bold)
        if (isVirtual) {
            parts.add(Message.translation("server.items.dynamic." + itemId + ".name").bold(true).color(DEFAULT_COLOR));
        } else if (item != null && item.getTranslationKey() != null) {
            parts.add(Message.translation(item.getTranslationKey()).bold(true).color(DEFAULT_COLOR));
        } else {
            parts.add(Message.raw(baseId.replace('_', ' ')).bold(true).color(DEFAULT_COLOR));
        }

        // Quantity
        if (quantity > 1) {
            parts.add(Message.raw(" x" + quantity).color(DEFAULT_COLOR));
        }

        // Stat modifiers (weapon/armor/utility)
        if (item != null) {
            appendStatModifiers(parts, item);
        }

        // Description (lore) - process tags server-side instead of relying on UI native formatting
        String finalLore = null;
        String originalDesc = "";

        if (item != null) {
            String descKey = item.getDescriptionTranslationKey();
            if (descKey != null && !descKey.isEmpty()) {
                I18nModule i18n = I18nModule.get();
                if (i18n != null) {
                    String msg = i18n.getMessage(locale, descKey);
                    if (msg != null && !msg.equals(descKey)) {
                        originalDesc = msg;
                    }
                }
            }
        }
        
        finalLore = originalDesc;

        if (isVirtual) {
            try {
                org.herolias.tooltips.api.DynamicTooltipsApi api = org.herolias.tooltips.api.DynamicTooltipsApiProvider.get();
                if (api != null) {
                    java.lang.reflect.Field registryField = api.getClass().getDeclaredField("registry");
                    registryField.setAccessible(true);
                    Object registry = registryField.get(api);
                    
                    java.lang.reflect.Method getComposed = registry.getClass().getMethod("getComposed", String.class);
                    String hash = itemId.substring(itemId.indexOf(VIRTUAL_SEPARATOR) + VIRTUAL_SEPARATOR.length());
                    Object composed = getComposed.invoke(registry, hash);
                    
                    if (composed != null) {
                        java.lang.reflect.Method buildDesc = composed.getClass().getMethod("buildDescription", String.class);
                        finalLore = (String) buildDesc.invoke(composed, originalDesc);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (finalLore != null && !finalLore.isEmpty()) {
            parts.add(Message.raw("\n"));
            List<Message> loreParts = parseColoredText(finalLore, LORE_COLOR);
            for (Message lorePart : loreParts) {
                parts.add(lorePart.italic(true));
            }
        }

        return Message.join(parts.toArray(new Message[0]));
    }

    /**
     * Returns just the translated item name (with quantity), for use in labels.
     */
    @Nonnull
    public static Message getTranslatedName(@Nonnull String itemId, int quantity) {
        String baseId = itemId;
        boolean isVirtual = false;
        if (itemId.contains(VIRTUAL_SEPARATOR)) {
            isVirtual = true;
            baseId = itemId.substring(0, itemId.indexOf(VIRTUAL_SEPARATOR));
        }

        Item item = null;
        try {
            item = (Item) Item.getAssetMap().getAsset(baseId);
        } catch (Exception ignored) {}

        Message name;
        if (isVirtual) {
            name = Message.translation("server.items.dynamic." + itemId + ".name");
        } else if (item != null && item.getTranslationKey() != null) {
            name = Message.translation(item.getTranslationKey());
        } else {
            name = Message.raw(baseId.replace('_', ' '));
        }

        if (quantity > 1) {
            name = Message.join(name, Message.raw(" x" + quantity));
        }
        return name;
    }

    /**
     * Appends weapon, armor, and utility stat modifiers to the tooltip.
     */
    private static void appendStatModifiers(List<Message> parts, @Nonnull Item item) {
        try {
            ItemWeapon weapon = item.getWeapon();
            if (weapon != null && weapon.getStatModifiers() != null) {
                appendStatsFromMap(parts, weapon.getStatModifiers());
            }
            ItemArmor armor = item.getArmor();
            if (armor != null && armor.getStatModifiers() != null) {
                appendStatsFromMap(parts, armor.getStatModifiers());
            }
            ItemUtility utility = item.getUtility();
            if (utility != null && utility.getStatModifiers() != null) {
                appendStatsFromMap(parts, utility.getStatModifiers());
            }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    private static void appendStatsFromMap(List<Message> parts, Int2ObjectMap<StaticModifier[]> statModifiers) {
        for (Int2ObjectMap.Entry<StaticModifier[]> entry : statModifiers.int2ObjectEntrySet()) {
            int statIndex = entry.getIntKey();
            StaticModifier[] modifiers = entry.getValue();

            String statName;
            try {
                EntityStatType statType = (EntityStatType) EntityStatType.getAssetMap().getAsset(statIndex);
                statName = statType != null ? statType.getId() : "Stat_" + statIndex;
            } catch (Exception e) {
                statName = "Stat_" + statIndex;
            }

            String displayName = humanizeStatName(statName);
            for (StaticModifier mod : modifiers) {
                float amount = mod.getAmount();
                String formattedValue;
                if (mod.getCalculationType() == StaticModifier.CalculationType.MULTIPLICATIVE) {
                    int pct = Math.round((amount - 1.0f) * 100.0f);
                    formattedValue = (pct >= 0 ? "+" : "") + pct + "%";
                } else {
                    String sign = amount >= 0.0f ? "+" : "";
                    formattedValue = (double) amount == Math.floor(amount)
                            ? sign + (int) amount
                            : sign + String.format("%.1f", amount);
                }
                parts.add(Message.raw("\n"));
                parts.add(Message.raw(displayName + ": ").color(STAT_COLOR));
                parts.add(Message.raw(formattedValue).color(STAT_VALUE_COLOR));
            }
        }
    }

    /**
     * Parses text containing {@code <color is="#hex">content</color>} tags
     * into a list of colored Message parts.
     */
    @Nonnull
    static List<Message> parseColoredText(String text, String defaultColor) {
        List<Message> result = new ArrayList<>();
        // Strip escaped backslashes
        text = text.replace("\\", "");
        // Also strip <i> and </i> tags (italic handled at call site)
        text = text.replace("<i>", "").replace("</i>", "");

        int pos = 0;
        while (pos < text.length()) {
            int tagStart = text.indexOf("<color", pos);
            if (tagStart < 0) {
                String remainder = text.substring(pos).trim();
                if (!remainder.isEmpty()) {
                    result.add(Message.raw(remainder).color(defaultColor));
                }
                break;
            }
            // Text before the tag
            if (tagStart > pos) {
                String before = text.substring(pos, tagStart).replaceAll("  +", " ");
                if (!before.trim().isEmpty()) {
                    result.add(Message.raw(before).color(defaultColor));
                }
            }
            // Find end of opening tag
            int tagEnd = text.indexOf('>', tagStart);
            if (tagEnd < 0) break;

            String tag = text.substring(tagStart, tagEnd + 1);
            String color = extractColorFromTag(tag);

            // Find closing tag
            int closeStart = text.indexOf("</color>", tagEnd);
            if (closeStart < 0) {
                String content = text.substring(tagEnd + 1).trim();
                if (!content.isEmpty()) {
                    result.add(Message.raw(content).color(color != null ? color : defaultColor));
                }
                break;
            }
            String innerText = text.substring(tagEnd + 1, closeStart);
            if (!innerText.trim().isEmpty()) {
                result.add(Message.raw(innerText).color(color != null ? color : defaultColor));
            }
            pos = closeStart + "</color>".length();
        }
        return result;
    }

    @Nullable
    private static String extractColorFromTag(String tag) {
        int hashIdx = tag.indexOf('#');
        if (hashIdx < 0) return null;
        int end = hashIdx + 1;
        while (end < tag.length() && isHexChar(tag.charAt(end))) {
            end++;
        }
        if (end > hashIdx + 1) {
            return tag.substring(hashIdx, end);
        }
        return null;
    }

    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    @Nonnull
    private static String humanizeStatName(String name) {
        name = name.replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && Character.isLowerCase(name.charAt(i - 1))) {
                sb.append(' ');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}

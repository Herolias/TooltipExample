package org.herolias.plugin;

import com.hypixel.hytale.protocol.CalculationType;
import com.hypixel.hytale.protocol.Modifier;
import com.hypixel.hytale.protocol.ModifierTarget;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import org.herolias.tooltips.api.ItemVisualOverrides;
import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Example provider that demonstrates the <b>additive</b> armor stat modifier API.
 * <p>
 * When an item has an {@code "armor_override"} metadata key, this provider
 * <b>adds</b> a bonus Health modifier to the item's tooltip — preserving
 * any existing stat modifiers the item already has.
 * <p>
 * Usage: {@code /setArmor head|chest|hands|legs}
 */
public class ArmorOverrideTooltipProvider implements TooltipProvider {

    @Nonnull
    @Override
    public String getProviderId() {
        return "tooltip-example:armor-override";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT + 20;
    }

    @Nullable
    @Override
    public TooltipData getTooltipData(@Nonnull String itemId, @Nullable String metadata) {
        if (metadata == null) return null;

        String slotStr = MetadataUtil.extractStringValue(metadata, "armor_override");
        if (slotStr == null) return null;

        // ── Additive API ──
        //
        // .addArmorStatModifier(statIndex, modifier) APPENDS to the original
        // item's existing modifiers — nothing is replaced.
        //
        // Use DefaultEntityStatTypes to get the correct runtime stat index.
        //
        int healthIndex = DefaultEntityStatTypes.getHealth();
        int manaIndex = DefaultEntityStatTypes.getMana();
        Modifier bonusHealth = new Modifier(ModifierTarget.Max, CalculationType.Additive, 5.0f);
        Modifier bonusMana = new Modifier(ModifierTarget.Max, CalculationType.Additive, 10.0f);

        ItemVisualOverrides overrides = ItemVisualOverrides.builder()
                .addArmorStatModifier(healthIndex, bonusHealth)
                .addArmorStatModifier(manaIndex, bonusMana)
                .build();

        return TooltipData.builder()
                .hashInput("armor_override:" + slotStr)
                .visualOverrides(overrides)
                .addLine("<color is=\"#8888FF\">• +5 Bonus Health (additive override)</color>")
                .build();
    }
}

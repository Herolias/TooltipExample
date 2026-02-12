package org.herolias.plugin;

import org.herolias.tooltips.api.ItemVisualOverrides;
import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Example provider that applies visual overrides based on a metadata key.
 * <p>
 * If the item has the metadata key {@code morph_visuals} set to {@code "adamantite_longsword"},
 * this provider will override its model, texture, and icon to look like an Adamantite Longsword.
 */
public class MorphTooltipProvider implements TooltipProvider {

    @Override
    public String getProviderId() {
        return "tooltip-example:morph";
    }

    @Override
    public int getPriority() {
        // High priority so our visual overrides take precedence
        return TooltipPriority.DEFAULT + 50; 
    }

    @Override
    public TooltipData getTooltipData(String itemId, String metadata) {
        if (metadata == null) return null;

        String morphTarget = MetadataUtil.extractStringValue(metadata, "morph_visuals");
        if (!"adamantite_longsword".equals(morphTarget)) {
            return null;
        }

        return TooltipData.builder()
                // IMPORTANT: The hash input must include the visual state so the virtual ID changes!
                .hashInput("morph:adamantite_longsword")
                
                .addLine("<color is=\"#FF55FF\">Morphed: Adamantite Longsword</color>")
                
                .visualOverrides(ItemVisualOverrides.builder()
                        .model("Items/Weapons/Longsword/Adamantite.blockymodel")
                        .texture("Items/Weapons/Longsword/Adamantite_Texture.png")
                        .icon("Icons/ItemsGenerated/Weapon_Longsword_Adamantite.png")
                        .build())
                .build();
    }
}

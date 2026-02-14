package org.herolias.plugin;

import org.herolias.tooltips.api.ItemVisualOverrides;
import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import javax.annotation.Nonnull;

/**
 * Provider that applies rarity visual overrides based on metadata.
 */
public class RarityTooltipProvider implements TooltipProvider {

    @Override
    public String getProviderId() {
        return "tooltip-example:rarity";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT + 10;
    }

    @Override
    public TooltipData getTooltipData(String itemId, String metadata) {
        if (metadata == null) return null;

        String rarityStr = MetadataUtil.extractStringValue(metadata, "rarity_override");
        if (rarityStr == null) return null;

        int qualityIndex;
        try {
            qualityIndex = Integer.parseInt(rarityStr);
        } catch (NumberFormatException e) {
            return null;
        }
        if(qualityIndex < 0 || qualityIndex > 10) { //rarity index below 0 crashes the client
            return null;
        }

        return TooltipData.builder()
                .hashInput("rarity:" + qualityIndex)
                .visualOverrides(ItemVisualOverrides.builder()
                        .qualityIndex(qualityIndex)
                        .build())
                .build();
    }
}

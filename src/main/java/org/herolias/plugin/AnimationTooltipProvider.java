package org.herolias.plugin;

import org.herolias.tooltips.api.ItemVisualOverrides;
import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import javax.annotation.Nonnull;

/**
 * Provider that applies animation visual overrides based on metadata.
 */
public class AnimationTooltipProvider implements TooltipProvider {

    @Override
    public String getProviderId() {
        return "tooltip-example:animation";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT + 10;
    }

    @Override
    public TooltipData getTooltipData(String itemId, String metadata) {
        if (metadata == null) return null;

        String playerAnim = MetadataUtil.extractStringValue(metadata, "change_player_anim");
        String forceAnimOff = MetadataUtil.extractStringValue(metadata, "anim_off");

        boolean hasPlayerAnim = "true".equals(playerAnim);
        boolean hasAnimOff = "true".equals(forceAnimOff);

        if (!hasPlayerAnim && !hasAnimOff) {
            return null;
        }

        ItemVisualOverrides.Builder visualBuilder = ItemVisualOverrides.builder();
        TooltipData.Builder dataBuilder = TooltipData.builder();
        StringBuilder hashBuilder = new StringBuilder();

        if (hasPlayerAnim) {
            visualBuilder.playerAnimationsId("pickaxe");
            visualBuilder.usePlayerAnimations(true);
            hashBuilder.append("player_anim:pickaxe;");
        }

        if (hasAnimOff) {
            visualBuilder.animation("idle");
            hashBuilder.append("item_anim:off;");
        }

        return dataBuilder
                .hashInput(hashBuilder.toString())
                .visualOverrides(visualBuilder.build())
                .build();
    }
}

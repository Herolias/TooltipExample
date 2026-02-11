package org.herolias.plugin;

import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tooltip provider that overrides an item's display name.
 * <p>
 * Reads the {@code rename} key from item metadata. When present, the item's
 * name in the tooltip is replaced with the stored value.
 * <p>
 * This demonstrates the <b>name override</b> (destructive) feature of
 * {@link TooltipData}.
 */
public class RenameTooltipProvider implements TooltipProvider {

    @Nonnull
    @Override
    public String getProviderId() {
        return "tooltip-example:rename";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT;
    }

    @Nullable
    @Override
    public TooltipData getTooltipData(@Nonnull String itemId, @Nullable String metadata) {
        if (metadata == null || !metadata.contains("rename")) return null;

        // The metadata string is BSON serialized as JSON, e.g.:
        // {"rename": "My Custom Sword", "other_key": "value"}
        String customName = MetadataUtil.extractStringValue(metadata, "rename");
        if (customName == null) return null;

        return TooltipData.builder()
                .nameOverride(customName)
                .hashInput("rename:" + customName)
                .build();
    }
}

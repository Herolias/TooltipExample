package org.herolias.plugin;

import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tooltip provider that adds custom lines or replaces the description.
 * <p>
 * Supports two metadata keys:
 * <ul>
 *   <li>{@code tooltip_lines} — pipe-separated additive lines (e.g. "Line 1|Line 2")</li>
 *   <li>{@code tooltip_desc} — a full description override that replaces everything</li>
 * </ul>
 * <p>
 * This demonstrates both the <b>additive lines</b> and <b>description override</b>
 * (destructive) features of {@link TooltipData}.
 */
public class CustomTooltipProvider implements TooltipProvider {

    @Nonnull
    @Override
    public String getProviderId() {
        return "tooltip-example:custom";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT;
    }

    @Nullable
    @Override
    public TooltipData getTooltipData(@Nonnull String itemId, @Nullable String metadata) {
        if (metadata == null) return null;

        String tooltipDesc = MetadataUtil.extractStringValue(metadata, "tooltip_desc");
        String tooltipLines = MetadataUtil.extractStringValue(metadata, "tooltip_lines");

        // Nothing for us to do
        if (tooltipDesc == null && tooltipLines == null) return null;

        TooltipData.Builder builder = TooltipData.builder();

        if (tooltipDesc != null) {
            // ── Description override mode ──
            // Replaces the ENTIRE tooltip description (destructive).
            builder.descriptionOverride("<color is=\"#AAAAFF\">" + tooltipDesc + "</color>");
            builder.hashInput("desc:" + tooltipDesc);
        } else {
            // ── Additive lines mode ──
            // Each line is appended after the original description.
            String[] lines = tooltipLines.split("\\|");
            for (String line : lines) {
                builder.addLine("<color is=\"#55FF55\">• " + line + "</color>");
            }
            builder.hashInput("lines:" + tooltipLines);
        }

        return builder.build();
    }
}

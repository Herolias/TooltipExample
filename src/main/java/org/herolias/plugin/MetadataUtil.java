package org.herolias.plugin;

import org.bson.BsonDocument;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Simple utility to extract string values from BSON metadata.
 * <p>
 * The DynamicTooltipsLib passes item metadata as a raw BSON/JSON string
 * to {@code TooltipProvider.getTooltipData()}. This helper parses it
 * and extracts individual string keys.
 */
public final class MetadataUtil {

    private MetadataUtil() {}

    /**
     * Extracts a string value from a BSON-serialized metadata string.
     *
     * @param metadata the raw metadata string (BSON/JSON format)
     * @param key      the key to look up
     * @return the string value, or {@code null} if not found or not a string
     */
    @Nullable
    public static String extractStringValue(@Nonnull String metadata, @Nonnull String key) {
        try {
            BsonDocument doc = BsonDocument.parse(metadata);
            BsonValue value = doc.get(key);
            if (value != null && value.isString()) {
                return value.asString().getValue();
            }
        } catch (Exception ignored) {
            // Metadata is not valid BSON/JSON — nothing to extract
        }
        return null;
    }
}

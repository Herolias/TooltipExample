package org.herolias.plugin;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Event data codec for the Shared List page (/openList).
 */
public class SharedListEventData {
    public static final BuilderCodec<SharedListEventData> CODEC = BuilderCodec.builder(
            SharedListEventData.class,
            SharedListEventData::new
        )
        .addField(new KeyedCodec<>("ReturnAll", Codec.STRING),
            (entry, s) -> entry.returnAll = s, entry -> entry.returnAll)
        .addField(new KeyedCodec<>("Close", Codec.STRING),
            (entry, s) -> entry.close = s, entry -> entry.close)
        .build();

    public String returnAll;
    public String close;

    public SharedListEventData() {
    }
}

package org.herolias.plugin;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Event data codec for the Inventory Grid UI page (/testUI).
 */
public class InventoryUIEventData {
    public static final BuilderCodec<InventoryUIEventData> CODEC = BuilderCodec.builder(
            InventoryUIEventData.class,
            InventoryUIEventData::new
        )
        .addField(new KeyedCodec<>("SlotClick", Codec.STRING),
            (entry, s) -> entry.slotClick = s, entry -> entry.slotClick)
        .addField(new KeyedCodec<>("Confirm", Codec.STRING),
            (entry, s) -> entry.confirm = s, entry -> entry.confirm)
        .addField(new KeyedCodec<>("Close", Codec.STRING),
            (entry, s) -> entry.close = s, entry -> entry.close)
        .build();

    public String slotClick;
    public String confirm;
    public String close;

    public InventoryUIEventData() {
    }
}

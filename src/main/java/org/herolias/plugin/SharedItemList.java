package org.herolias.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages a persistent shared item list stored as JSON on disk.
 * Thread-safe — multiple players can add/remove items concurrently.
 */
public class SharedItemList {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<SerializedItem>>() {}.getType();

    private final Path filePath;
    private final List<SerializedItem> items = new ArrayList<>();

    /**
     * A simple POJO representing a serialized item for JSON persistence.
     */
    public static class SerializedItem {
        public String itemId;
        public int quantity;
        public double durability;
        public double maxDurability;
        public String metadata; // JSON string or null

        public SerializedItem() {}

        public SerializedItem(String itemId, int quantity, double durability, double maxDurability, String metadata) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.durability = durability;
            this.maxDurability = maxDurability;
            this.metadata = metadata;
        }

        /**
         * Creates a SerializedItem from an ItemStack.
         */
        public static SerializedItem fromItemStack(@Nonnull ItemStack stack) {
            org.bson.BsonDocument meta = stack.getMetadata();
            return new SerializedItem(
                stack.getItemId(),
                stack.getQuantity(),
                stack.getDurability(),
                stack.getMaxDurability(),
                meta != null ? meta.toJson() : null
            );
        }

        /**
         * Converts this SerializedItem back to an ItemStack.
         */
        @Nonnull
        public ItemStack toItemStack() {
            org.bson.BsonDocument meta = null;
            if (metadata != null && !metadata.isEmpty()) {
                meta = org.bson.BsonDocument.parse(metadata);
            }
            return new ItemStack(itemId, quantity, durability, maxDurability, meta);
        }
    }

    private SharedItemList(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads (or creates) the shared item list from disk.
     */
    @Nonnull
    public static SharedItemList load(@Nonnull Path dataDir) {
        Path file = dataDir.resolve("shared_items.json");
        SharedItemList list = new SharedItemList(file);

        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                List<SerializedItem> loaded = GSON.fromJson(json, LIST_TYPE);
                if (loaded != null) {
                    list.items.addAll(loaded);
                }
                LOGGER.atInfo().log("SharedItemList: Loaded %d items from %s", list.items.size(), file);
            } catch (IOException e) {
                LOGGER.atSevere().log("SharedItemList: Failed to load from %s: %s", file, e.getMessage());
            }
        } else {
            LOGGER.atInfo().log("SharedItemList: No existing file at %s, starting empty.", file);
        }

        return list;
    }

    /**
     * Saves the current list to disk.
     */
    public synchronized void save() {
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, GSON.toJson(items, LIST_TYPE));
        } catch (IOException e) {
            LOGGER.atSevere().log("SharedItemList: Failed to save to %s: %s", filePath, e.getMessage());
        }
    }

    /**
     * Adds an item to the shared list and saves.
     */
    public synchronized void addItem(@Nonnull ItemStack stack) {
        items.add(SerializedItem.fromItemStack(stack));
        save();
    }

    /**
     * Returns an unmodifiable snapshot of the current items.
     */
    @Nonnull
    public synchronized List<SerializedItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    /**
     * Removes all items from the list and saves. Returns the removed items.
     */
    @Nonnull
    public synchronized List<SerializedItem> removeAll() {
        List<SerializedItem> removed = new ArrayList<>(items);
        items.clear();
        save();
        return removed;
    }

    /**
     * Returns true if the list is empty.
     */
    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns the number of items in the list.
     */
    public synchronized int size() {
        return items.size();
    }
}

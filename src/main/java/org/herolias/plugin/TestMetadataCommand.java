package org.herolias.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /testMetadata — Adds a "Test" entry to the metadata of the held item.
 * <p>
 * This command is executed asynchronously on the World thread to ensure
 * inventory updates are thread-safe.
 */
public class TestMetadataCommand extends AbstractAsyncCommand {

    private final TooltipExample plugin;

    public TestMetadataCommand(TooltipExample plugin) {
        super("testMetadata", "Add test metadata to the held item.");
        this.plugin = plugin;
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = playerRef.getStore();
        World world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player == null) return;

            ItemStack heldItem = player.getInventory().getItemInHand();
            if (heldItem == null || heldItem.isEmpty()) {
                player.sendMessage(Message.raw("You must be holding an item!"));
                return;
            }

            // Add "Test": "True" to the item's metadata
            BsonDocument metadata = heldItem.getMetadata();
            if (metadata == null) metadata = new BsonDocument();
            metadata.put("Test", new BsonString("True"));

            // Create a new ItemStack with updated metadata and place it back
            ItemStack updated = heldItem.withMetadata(metadata);
            player.getInventory().getHotbar().setItemStackForSlot(
                    (short) player.getInventory().getActiveHotbarSlot(), updated);
            player.getInventory().markChanged();
            player.sendInventory();

            // Refresh tooltips so the change is potentially visible if a provider uses it
            plugin.getTooltipsApi().refreshPlayer(player.getUuid());
            player.sendMessage(Message.raw("Added 'Test' metadata to item."));
        }, world);
    }
}

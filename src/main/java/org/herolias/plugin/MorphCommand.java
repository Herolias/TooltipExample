package org.herolias.plugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.annotation.Nonnull;

/**
 * /morph — Toggles the visual appearance of the held item to look like an Adamantite Longsword.
 */
public class MorphCommand extends CommandBase {

    private final TooltipExample plugin;

    public MorphCommand(TooltipExample plugin) {
        super("morph", "Toggles the visual morph of the held item.");
        this.plugin = plugin;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Message.raw("Only players can use this command."));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        BsonDocument metadata = heldItem.getMetadata();
        if (metadata == null) metadata = new BsonDocument();

        String existingMorph = MetadataUtil.extractStringValue(metadata.toJson(), "morph_visuals");

        boolean turningOn = true;
        
        if ("adamantite_longsword".equals(existingMorph)) {
            // Toggle OFF
            metadata.remove("morph_visuals");
            turningOn = false;
        } else {
            // Toggle ON
            metadata.put("morph_visuals", new BsonString("adamantite_longsword"));
        }

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);
        player.getInventory().markChanged();
        player.sendInventory();

        // Refresh tooltips for player
        plugin.getTooltipsApi().refreshPlayer(player.getUuid());

        if (turningOn) {
            sender.sendMessage(Message.raw("Item morphed to Adamantite Longsword (Client-side Visual Only)!"));
        } else {
            sender.sendMessage(Message.raw("Item morph removed."));
        }
    }
}

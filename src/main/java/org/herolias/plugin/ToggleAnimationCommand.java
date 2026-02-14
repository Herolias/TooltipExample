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
 * /toggleAnimation — Toggles the animation of the Item held on or off.
 */
public class ToggleAnimationCommand extends CommandBase {

    private final TooltipExample plugin;

    public ToggleAnimationCommand(TooltipExample plugin) {
        super("toggleAnimation", "Toggles the item's animation on or off.");
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

        String existing = MetadataUtil.extractStringValue(metadata.toJson(), "anim_off");
        boolean isOff = "true".equals(existing);

        if (isOff) {
            // It was OFF, turn it ON (remove the override)
            metadata.remove("anim_off");
        } else {
            // It was ON (or default), turn it OFF
            metadata.put("anim_off", new BsonString("true"));
        }

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);
        player.getInventory().markChanged();
        player.sendInventory();

        // Refresh tooltips for player
        plugin.getTooltipsApi().refreshPlayer(player.getUuid());

        if (isOff) {
            sender.sendMessage(Message.raw("Item animation: ENABLED (default)"));
        } else {
            sender.sendMessage(Message.raw("Item animation: DISABLED (forced idle)"));
        }
    }
}

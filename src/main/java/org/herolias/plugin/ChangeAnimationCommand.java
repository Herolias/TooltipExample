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
 * /changeAnimation — Toggles the Playeranimation of the item in hand to the animation of a pickaxe.
 */
public class ChangeAnimationCommand extends CommandBase {

    private final TooltipExample plugin;

    public ChangeAnimationCommand(TooltipExample plugin) {
        super("changeAnimation", "Toggles the player animation of the held item (pickaxe hold).");
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

        String existing = MetadataUtil.extractStringValue(metadata.toJson(), "change_player_anim");
        boolean turningOn = true;

        if ("true".equals(existing)) {
            // Toggle OFF
            metadata.remove("change_player_anim");
            turningOn = false;
        } else {
            // Toggle ON
            metadata.put("change_player_anim", new BsonString("true"));
        }

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);
        player.getInventory().markChanged();
        player.sendInventory();

        // Refresh tooltips for player
        plugin.getTooltipsApi().refreshPlayer(player.getUuid());

        if (turningOn) {
            sender.sendMessage(Message.raw("Player animation changed to Pickaxe hold!"));
        } else {
            sender.sendMessage(Message.raw("Player animation reverted to default."));
        }
    }
}

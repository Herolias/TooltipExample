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
 * /rename &lt;name&gt; — Sets a custom display name on the held item.
 * <p>
 * Demonstrates: {@code TooltipData.builder().nameOverride(name)}
 */
public class RenameCommand extends CommandBase {

    private final TooltipExample plugin;

    public RenameCommand(TooltipExample plugin) {
        super("rename", "Rename the held item. Usage: /rename <name>");
        this.setAllowsExtraArguments(true);
        this.plugin = plugin;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Message.raw("Only players can use this command."));
            return;
        }

        String rawInput = context.getInputString();
        // getInputString() includes the command name as the first word — strip it
        String args = stripCommandName(rawInput);
        if (args == null || args.isBlank()) {
            sender.sendMessage(Message.raw("Usage: /rename <name>"));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        String customName = args.trim();

        // Write the custom name into the item's metadata
        BsonDocument metadata = heldItem.getMetadata();
        if (metadata == null) metadata = new BsonDocument();
        metadata.put("rename", new BsonString(customName));

        // Create a new ItemStack with updated metadata and place it back
        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);

        // Refresh tooltips so the change is visible immediately
        plugin.getTooltipsApi().refreshPlayer(player.getUuid());
        sender.sendMessage(Message.raw("Item renamed to: " + customName));
    }

    private static String stripCommandName(String input) {
        if (input == null) return null;
        int space = input.indexOf(' ');
        return space >= 0 ? input.substring(space + 1) : null;
    }
}

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
 * /replaceTooltip &lt;text&gt; — Replaces the entire tooltip description.
 * <p>
 * Demonstrates: {@code TooltipData.builder().descriptionOverride(text)}
 */
public class ReplaceTooltipCommand extends CommandBase {

    private final TooltipExample plugin;

    public ReplaceTooltipCommand(TooltipExample plugin) {
        super("replaceTooltip", "Replace the held item's tooltip. Usage: /replaceTooltip <text>");
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
        String args = stripCommandName(rawInput);
        if (args == null || args.isBlank()) {
            sender.sendMessage(Message.raw("Usage: /replaceTooltip <text>"));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        String newDesc = args.trim();

        // Set a full description override in metadata
        BsonDocument metadata = heldItem.getMetadata();
        if (metadata == null) metadata = new BsonDocument();
        metadata.put("tooltip_desc", new BsonString(newDesc));

        // Remove additive lines so the override is clean
        metadata.remove("tooltip_lines");

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);
        player.getInventory().markChanged();
        player.sendInventory();

        plugin.getTooltipsApi().refreshPlayer(player.getUuid());
        sender.sendMessage(Message.raw("Tooltip description replaced!"));
    }

    private static String stripCommandName(String input) {
        if (input == null) return null;
        int space = input.indexOf(' ');
        return space >= 0 ? input.substring(space + 1) : null;
    }
}

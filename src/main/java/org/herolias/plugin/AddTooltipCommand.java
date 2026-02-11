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
 * /addTooltip &lt;text&gt; — Appends a line to the held item's tooltip.
 * <p>
 * Demonstrates: {@code TooltipData.builder().addLine(text)}
 */
public class AddTooltipCommand extends CommandBase {

    private final TooltipExample plugin;

    public AddTooltipCommand(TooltipExample plugin) {
        super("addTooltip", "Add a tooltip line to the held item. Usage: /addTooltip <text>");
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
            sender.sendMessage(Message.raw("Usage: /addTooltip <text>"));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        String newLine = args.trim();

        // Append the new line to existing tooltip_lines in metadata
        BsonDocument metadata = heldItem.getMetadata();
        if (metadata == null) metadata = new BsonDocument();

        String existing = metadata.containsKey("tooltip_lines")
                ? metadata.getString("tooltip_lines").getValue()
                : null;
        String combined = (existing != null) ? existing + "|" + newLine : newLine;
        metadata.put("tooltip_lines", new BsonString(combined));

        // Remove any description override so additive lines are shown
        metadata.remove("tooltip_desc");

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);
        player.getInventory().markChanged();
        player.sendInventory();

        plugin.getTooltipsApi().refreshPlayer(player.getUuid());
        sender.sendMessage(Message.raw("Added tooltip line: " + newLine));
    }

    private static String stripCommandName(String input) {
        if (input == null) return null;
        int space = input.indexOf(' ');
        return space >= 0 ? input.substring(space + 1) : null;
    }
}

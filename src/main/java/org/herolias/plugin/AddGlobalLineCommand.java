package org.herolias.plugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * /addGlobalLine &lt;text&gt; — Appends a line to the global tooltip of the held item's base type.
 * <p>
 * Demonstrates: {@code DynamicTooltipsApi.addGlobalLine(baseItemId, text)}
 */
public class AddGlobalLineCommand extends CommandBase {

    private final TooltipExample plugin;

    public AddGlobalLineCommand(TooltipExample plugin) {
        super("addGlobalLine", "Add a global tooltip line to the held item's base type. Usage: /addGlobalLine <text>");
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
            sender.sendMessage(Message.raw("Usage: /addGlobalLine <text>"));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        String baseItemId = heldItem.getItemId();
        // Call the global API
        plugin.getTooltipsApi().addGlobalLine(baseItemId, args.trim());

        sender.sendMessage(Message.raw("Added global tooltip line to " + baseItemId + ": " + args.trim()));
    }

    private static String stripCommandName(String input) {
        if (input == null) return null;
        int space = input.indexOf(' ');
        return space >= 0 ? input.substring(space + 1) : null;
    }
}

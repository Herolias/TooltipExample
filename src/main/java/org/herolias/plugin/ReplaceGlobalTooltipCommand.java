package org.herolias.plugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * /replaceGlobalTooltip &lt;text&gt; — Replaces the global tooltip of the held item's base type.
 * <p>
 * Demonstrates: {@code DynamicTooltipsApi.replaceGlobalTooltip(baseItemId, text)}
 */
public class ReplaceGlobalTooltipCommand extends CommandBase {

    private final TooltipExample plugin;

    public ReplaceGlobalTooltipCommand(TooltipExample plugin) {
        super("replaceGlobalTooltip", "Replace the global tooltip of the held item's base type. Usage: /replaceGlobalTooltip <text>");
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

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        String baseItemId = heldItem.getItemId();

        if (args == null || args.isBlank()) {
            // Treat empty args as a clear
            plugin.getTooltipsApi().clearGlobalTooltips(baseItemId);
            sender.sendMessage(Message.raw("Cleared global tooltips for " + baseItemId));
            return;
        }

        // Call the global API
        plugin.getTooltipsApi().replaceGlobalTooltip(baseItemId, args.trim());

        sender.sendMessage(Message.raw("Replaced global tooltip for " + baseItemId + " with: " + args.trim()));
    }

    private static String stripCommandName(String input) {
        if (input == null) return null;
        int space = input.indexOf(' ');
        return space >= 0 ? input.substring(space + 1) : null;
    }
}

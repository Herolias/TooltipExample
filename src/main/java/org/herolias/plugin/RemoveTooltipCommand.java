package org.herolias.plugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import org.bson.BsonDocument;

import javax.annotation.Nonnull;

/**
 * /removeTooltip &lt;type&gt; — Removes tooltip data from the held item.
 * <p>
 * Types:
 * <ul>
 *   <li>{@code rename} — removes the custom name override</li>
 *   <li>{@code lines} — removes all additive tooltip lines</li>
 *   <li>{@code desc} — removes the description override</li>
 *   <li>{@code all} — removes all tooltip modifications</li>
 * </ul>
 */
public class RemoveTooltipCommand extends CommandBase {

    private final TooltipExample plugin;

    public RemoveTooltipCommand(TooltipExample plugin) {
        super("removeTooltip", "Remove tooltip data. Usage: /removeTooltip <rename|lines|desc|all>");
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
            sender.sendMessage(Message.raw("Usage: /removeTooltip <rename|lines|desc|all>"));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        BsonDocument metadata = heldItem.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            sender.sendMessage(Message.raw("This item has no tooltip data to remove."));
            return;
        }

        String type = args.trim().toLowerCase();
        String removedWhat;

        switch (type) {
            case "rename" -> {
                if (!metadata.containsKey("rename")) {
                    sender.sendMessage(Message.raw("This item has no custom name."));
                    return;
                }
                metadata.remove("rename");
                removedWhat = "custom name";
            }
            case "lines" -> {
                if (!metadata.containsKey("tooltip_lines")) {
                    sender.sendMessage(Message.raw("This item has no tooltip lines."));
                    return;
                }
                metadata.remove("tooltip_lines");
                removedWhat = "tooltip lines";
            }
            case "desc" -> {
                if (!metadata.containsKey("tooltip_desc")) {
                    sender.sendMessage(Message.raw("This item has no description override."));
                    return;
                }
                metadata.remove("tooltip_desc");
                removedWhat = "description override";
            }
            case "all" -> {
                metadata.remove("rename");
                metadata.remove("tooltip_lines");
                metadata.remove("tooltip_desc");
                removedWhat = "all tooltip data";
            }
            default -> {
                sender.sendMessage(Message.raw("Unknown type '" + type + "'. Use: rename, lines, desc, or all"));
                return;
            }
        }

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);

        plugin.getTooltipsApi().refreshPlayer(player.getUuid());
        sender.sendMessage(Message.raw("Removed " + removedWhat + " from item."));
    }

    private static String stripCommandName(String input) {
        if (input == null) return null;
        int space = input.indexOf(' ');
        return space >= 0 ? input.substring(space + 1) : null;
    }
}

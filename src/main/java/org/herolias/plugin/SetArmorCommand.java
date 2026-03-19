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
 * /setArmor [slot] — Overrides the armor slot display on the held item's tooltip.
 * <p>
 * Valid slots: {@code head}, {@code chest}, {@code hands}, {@code legs}, {@code clear}
 * <p>
 * This is purely visual — it does not change the item's actual armor properties.
 */
public class SetArmorCommand extends CommandBase {

    private final TooltipExample plugin;

    public SetArmorCommand(TooltipExample plugin) {
        super("setArmor", "Overrides the armor slot display on the held item's tooltip.");
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

        ItemStack heldItem = player.getInventory().getItemInHand();
        if (heldItem == null || heldItem.isEmpty()) {
            sender.sendMessage(Message.raw("You must be holding an item!"));
            return;
        }

        String inputLine = context.getInputString();
        String[] parts = inputLine.split("\\s+");

        if (parts.length < 2) {
            sender.sendMessage(Message.raw("Usage: /setArmor <head|chest|hands|legs|clear>"));
            return;
        }

        String input = parts[1].toLowerCase();
        BsonDocument metadata = heldItem.getMetadata();
        if (metadata == null) metadata = new BsonDocument();

        if (input.equals("clear")) {
            metadata.remove("armor_override");
            sender.sendMessage(Message.raw("Armor override cleared."));
        } else {
            // Map user-friendly names to ItemArmorSlot enum names
            String slotName;
            switch (input) {
                case "head": slotName = "Head"; break;
                case "chest": slotName = "Chest"; break;
                case "hands": slotName = "Hands"; break;
                case "legs": slotName = "Legs"; break;
                default:
                    sender.sendMessage(Message.raw("Unknown slot: " + input + ". Use: head, chest, hands, legs, clear"));
                    return;
            }

            metadata.put("armor_override", new BsonString(slotName));
            sender.sendMessage(Message.raw("Armor slot override set to: " + slotName));
        }

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);

        plugin.getTooltipsApi().refreshPlayer(player.getUuid());
    }
}

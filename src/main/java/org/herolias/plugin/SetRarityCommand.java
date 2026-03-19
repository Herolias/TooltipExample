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
 * /setRarity [rarity] — Sets the visual rarity of the held item.
 */
public class SetRarityCommand extends CommandBase {

    private final TooltipExample plugin;

    public SetRarityCommand(TooltipExample plugin) {
        super("setRarity", "Sets the visual rarity of the held item.");
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
        
        if (parts.length < 2) { // 0 is command, 1 is arg
            sender.sendMessage(Message.raw("Usage: /setRarity <common|uncommon|rare|epic|legendary|developer|number>"));
            return;
        }

        String input = parts[1].toLowerCase();
        int qualityIndex = -1;

        switch (input) {
            case "common": qualityIndex = 0; break;
            case "uncommon": qualityIndex = 4; break;
            case "rare": qualityIndex = 5; break;
            case "epic": qualityIndex = 2; break;
            case "legendary": qualityIndex = 6; break;
            case "developer": qualityIndex = 3; break;
            default:
                try {
                    qualityIndex = Integer.parseInt(input);
                } catch (NumberFormatException ignored) {}
        }

        BsonDocument metadata = heldItem.getMetadata();
        if (metadata == null) metadata = new BsonDocument();

        // Store as string since MetadataUtil extracts strings
        metadata.put("rarity_override", new BsonString(String.valueOf(qualityIndex)));

        ItemStack updated = heldItem.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(
                (short) player.getInventory().getActiveHotbarSlot(), updated);

        // Refresh tooltips for player
        plugin.getTooltipsApi().refreshPlayer(player.getUuid());

        sender.sendMessage(Message.raw("Rarity set to: " + input + " (" + qualityIndex + ")"));
    }
}

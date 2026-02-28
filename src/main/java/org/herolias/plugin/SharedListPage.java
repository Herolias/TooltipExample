package org.herolias.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;

import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Interactive UI page for /openList.
 * Displays the shared item list with item icons and hover tooltips.
 * A "Return All" button puts all items back in the player's inventory.
 */
public class SharedListPage extends InteractiveCustomUIPage<SharedListEventData> {

    private final PlayerRef playerRef;
    private final SharedItemList sharedList;

    public SharedListPage(@Nonnull PlayerRef playerRef, @Nonnull SharedItemList sharedList) {
        super(playerRef, CustomPageLifetime.CanDismiss, SharedListEventData.CODEC);
        this.playerRef = playerRef;
        this.sharedList = sharedList;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder,
                      @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {

        commandBuilder.append("Pages/SharedListPage.ui");

        // Build the item list content
        buildItemList(commandBuilder);

        // Bind return all button
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnAllButton",
                EventData.of("ReturnAll", "true"));

        // Bind close button
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                EventData.of("Close", "true"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull SharedListEventData data) {
        if ("true".equals(data.returnAll)) {
            handleReturnAll(ref, store);
        } else if ("true".equals(data.close)) {
            closePage(ref, store);
        }
    }

    private void handleReturnAll(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (sharedList.isEmpty()) {
            playerRef.sendMessage(Message.raw("The shared list is empty!"));
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        Inventory inventory = player.getInventory();
        List<SharedItemList.SerializedItem> items = sharedList.removeAll();

        int returned = 0;
        int failed = 0;

        for (SharedItemList.SerializedItem serialized : items) {
            ItemStack stack = serialized.toItemStack();
            // Try to add to hotbar first, then storage
            if (inventory.getCombinedHotbarFirst().canAddItemStack(stack)) {
                inventory.getCombinedHotbarFirst().addItemStack(stack);
                returned++;
            } else {
                // Can't fit — add back to shared list
                sharedList.addItem(stack);
                failed++;
            }
        }

        inventory.markChanged();

        if (failed > 0) {
            playerRef.sendMessage(Message.raw("Returned " + returned + " item(s). " + failed + " item(s) couldn't fit and remain in the list."));
        } else {
            playerRef.sendMessage(Message.raw("Returned all " + returned + " item(s) to your inventory!"));
        }

        // Refresh UI
        UICommandBuilder commandBuilder = new UICommandBuilder();
        commandBuilder.clear("#ItemList");
        buildItemList(commandBuilder);
        this.sendUpdate(commandBuilder, null, false);
    }

    private void buildItemList(@Nonnull UICommandBuilder commandBuilder) {
        List<SharedItemList.SerializedItem> items = sharedList.getItems();

        if (items.isEmpty()) {
            commandBuilder.set("#EmptyLabel.Visible", true);
            return;
        }

        commandBuilder.set("#EmptyLabel.Visible", false);

        for (int i = 0; i < items.size(); i++) {
            SharedItemList.SerializedItem item = items.get(i);

            // Append an entry UI template
            commandBuilder.append("#ItemList", "Pages/SharedListEntry.ui");

            String entrySelector = "#ItemList[" + i + "]";

            // Create the full ItemStack to pass metadata to DynamicTooltipsLib,
            // which will intercept it, compose the tooltip, and then strip it
            // before the client crashes on ItemGridSlot ArrayCodec!
            ItemStack stack = item.toItemStack();
            Object[] slotArray = new ItemGridSlot[]{new ItemGridSlot(stack)};
            commandBuilder.set(entrySelector + " #ItemIcon.Slots", slotArray);

            // Set the item name label using translated text (name only)
            Message nameMessage = ItemTooltipHelper.getTranslatedName(item.itemId, item.quantity);
            commandBuilder.set(entrySelector + " #ItemName.TextSpans", nameMessage);
        }
    }

    private void closePage(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }
}

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

import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interactive UI page for /testUI.
 * Displays a grid of all inventory slots (4 storage rows + 1 hotbar row),
 * each showing the item icon via ItemGrid. Players click slots to toggle
 * selection, then confirm to move selected items to the shared list.
 */
public class InventoryUIPage extends InteractiveCustomUIPage<InventoryUIEventData> {

    private static final String SLOT_DOCUMENT = "Pages/InventorySlot.ui";
    private static final int SLOTS_PER_ROW = 9;
    private static final int STORAGE_ROWS = 4;
    private static final int HOTBAR_SIZE = 9;
    private static final int STORAGE_SIZE = 36;

    private final PlayerRef playerRef;
    private final SharedItemList sharedList;
    private final Set<Integer> selectedSlots = new HashSet<>();

    public InventoryUIPage(@Nonnull PlayerRef playerRef, @Nonnull SharedItemList sharedList) {
        super(playerRef, CustomPageLifetime.CanDismiss, InventoryUIEventData.CODEC);
        this.playerRef = playerRef;
        this.sharedList = sharedList;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder,
                      @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {

        commandBuilder.append("Pages/InventoryGridPage.ui");

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            Inventory inventory = player.getInventory();

            // Render 4 storage rows (slots 0-35)
            for (int row = 0; row < STORAGE_ROWS; row++) {
                String rowId = "#InvRow" + row;
                int startSlot = row * SLOTS_PER_ROW;
                renderInventoryRow(commandBuilder, eventBuilder, rowId, startSlot, SLOTS_PER_ROW, inventory, false);
            }

            // Render hotbar row (logical slots 36-44)
            renderInventoryRow(commandBuilder, eventBuilder, "#HotbarRow", STORAGE_SIZE, HOTBAR_SIZE, inventory, true);
        }

        // Bind confirm button
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmButton",
                EventData.of("Confirm", "true"));

        // Bind close button
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                EventData.of("Close", "true"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull InventoryUIEventData data) {
        if (data.slotClick != null) {
            handleSlotClick(ref, store, data.slotClick);
        } else if ("true".equals(data.confirm)) {
            handleConfirm(ref, store);
        } else if ("true".equals(data.close)) {
            closePage(ref, store);
        }
    }

    /**
     * Renders a row of inventory slots with item icons.
     */
    private void renderInventoryRow(@Nonnull UICommandBuilder commandBuilder,
                                     @Nonnull UIEventBuilder eventBuilder,
                                     @Nonnull String parentId,
                                     int startSlot, int count,
                                     @Nonnull Inventory inventory,
                                     boolean isHotbar) {
        commandBuilder.clear(parentId);

        for (int i = 0; i < count; i++) {
            int logicalIndex = startSlot + i;

            commandBuilder.append(parentId, SLOT_DOCUMENT);
            String selector = parentId + "[" + i + "]";

            // Get the item stack for this slot
            ItemStack stack = getItemStackAtSlot(inventory, logicalIndex);

            if (stack != null && !ItemStack.isEmpty(stack)) {
                // Use ItemGrid .Slots with full ItemGridSlot(ItemStack) so that
                // DynamicTooltipsLib receives the item's metadata in the CustomPage
                // packet and can compose per-item tooltips/visual overrides.
                //
                // Previously this used ItemSlot .ItemId (a bare string), which
                // stripped metadata. The lib's packet interceptor only saw a raw
                // item ID and had to fall back to findVirtualIdForItem(), which
                // searches the player's inventory slot tracking for ANY virtual ID
                // with the same base type — returning the wrong override for items
                // that share a base ID but have different enchantments/tooltips.
                Object[] slotArray = new com.hypixel.hytale.server.core.ui.ItemGridSlot[]{
                    new com.hypixel.hytale.server.core.ui.ItemGridSlot(stack)
                };
                commandBuilder.set(selector + " #ItemIcon.Slots", slotArray);

                // ItemGrid automatically displays quantity numbers natively.

                // Build rich tooltip with item name, stats, and description
                commandBuilder.set(selector + " #SlotBorder.TooltipTextSpans",
                        ItemTooltipHelper.buildTooltip(stack.getItemId(), stack.getQuantity(),
                                playerRef.getLanguage()));
            } else {
                // Empty slot — clear the icon (ItemGrid uses .Slots, not .ItemId)
                commandBuilder.set(selector + " #ItemIcon.Slots", new com.hypixel.hytale.server.core.ui.ItemGridSlot[0]);
            }

            // Selection overlay
            boolean isSelected = selectedSlots.contains(logicalIndex);
            commandBuilder.set(selector + " #SelectionOverlay.Visible", isSelected);

            // Bind click event with the logical slot index
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                    selector + " #SlotButton",
                    EventData.of("SlotClick", String.valueOf(logicalIndex)), false);
        }
    }

    /**
     * Gets an ItemStack from inventory by logical index.
     * Slots 0-35 = storage, 36-44 = hotbar.
     */
    private ItemStack getItemStackAtSlot(@Nonnull Inventory inventory, int logicalIndex) {
        if (logicalIndex < STORAGE_SIZE) {
            return inventory.getStorage().getItemStack((short) logicalIndex);
        } else if (logicalIndex < STORAGE_SIZE + HOTBAR_SIZE) {
            return inventory.getHotbar().getItemStack((short) (logicalIndex - STORAGE_SIZE));
        }
        return null;
    }


    private void handleSlotClick(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                  String slotIndexStr) {
        int logicalIndex;
        try {
            logicalIndex = Integer.parseInt(slotIndexStr);
        } catch (NumberFormatException e) {
            return;
        }

        if (logicalIndex < 0 || logicalIndex >= STORAGE_SIZE + HOTBAR_SIZE) return;

        // Only allow selecting non-empty slots
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        ItemStack stack = getItemStackAtSlot(player.getInventory(), logicalIndex);
        if (stack == null || ItemStack.isEmpty(stack)) return;

        // Toggle selection
        if (selectedSlots.contains(logicalIndex)) {
            selectedSlots.remove(logicalIndex);
        } else {
            selectedSlots.add(logicalIndex);
        }

        // Update the selection overlay
        UICommandBuilder commandBuilder = new UICommandBuilder();
        String selector = getSlotSelector(logicalIndex);
        commandBuilder.set(selector + " #SelectionOverlay.Visible", selectedSlots.contains(logicalIndex));
        this.sendUpdate(commandBuilder, null, false);
    }

    /**
     * Converts a logical slot index back to its UI selector.
     */
    private String getSlotSelector(int logicalIndex) {
        if (logicalIndex < STORAGE_SIZE) {
            int row = logicalIndex / SLOTS_PER_ROW;
            int col = logicalIndex % SLOTS_PER_ROW;
            return "#InvRow" + row + "[" + col + "]";
        } else {
            int col = logicalIndex - STORAGE_SIZE;
            return "#HotbarRow[" + col + "]";
        }
    }

    private void handleConfirm(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (selectedSlots.isEmpty()) {
            playerRef.sendMessage(Message.raw("No items selected!"));
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        Inventory inventory = player.getInventory();
        int count = 0;

        // Process selected items
        List<Integer> sortedSlots = new ArrayList<>(selectedSlots);
        sortedSlots.sort(Integer::compareTo);

        for (int logicalIndex : sortedSlots) {
            ItemStack item = getItemStackAtSlot(inventory, logicalIndex);

            if (item != null && !ItemStack.isEmpty(item)) {
                // Remove from inventory
                if (logicalIndex < STORAGE_SIZE) {
                    inventory.getStorage().removeItemStackFromSlot((short) logicalIndex, false);
                } else {
                    inventory.getHotbar().removeItemStackFromSlot((short) (logicalIndex - STORAGE_SIZE), false);
                }
                // Add to shared list
                sharedList.addItem(item);
                count++;
            }
        }

        playerRef.sendMessage(Message.raw("Added " + count + " item(s) to the shared list!"));
        closePage(ref, store);
    }

    private void closePage(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }
}

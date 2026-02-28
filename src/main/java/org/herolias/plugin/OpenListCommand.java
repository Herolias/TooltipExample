package org.herolias.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /openList — Opens the shared item list UI.
 * Shows all items added by any player, with a button to return them to inventory.
 */
public class OpenListCommand extends AbstractAsyncCommand {

    private static final Message MESSAGE_NOT_A_PLAYER = Message.raw("Only players can use this command.");

    private final TooltipExample plugin;

    public OpenListCommand(TooltipExample plugin) {
        super("openList", "Opens the shared item list.");
        this.plugin = plugin;
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(MESSAGE_NOT_A_PLAYER);
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = playerRef.getStore();
        World world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            Player playerComponent = store.getComponent(playerRef, Player.getComponentType());
            PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());

            if (playerComponent != null && playerRefComponent != null) {
                playerComponent.getPageManager().openCustomPage(
                    playerRef,
                    store,
                    new SharedListPage(playerRefComponent, plugin.getSharedItemList())
                );
            }
        }, world);
    }
}

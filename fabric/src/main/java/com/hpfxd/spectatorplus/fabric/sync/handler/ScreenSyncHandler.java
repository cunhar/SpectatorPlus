package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenSyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenSyncHandler.class);

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(ServerboundRequestInventoryOpenPacket.TYPE, ScreenSyncHandler::handle);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundOpenedInventorySyncPacket.TYPE, ScreenSyncHandler::handle);
    }

    private static void handle(ServerboundRequestInventoryOpenPacket packet, ServerPlayNetworking.Context ctx) {
        final var spectator = ctx.player();
        final var target = ctx.server().getPlayerList().getPlayer(packet.playerId());
        if (target == null) {
            return;
        }
        // Send screen sync packet to indicate survival inventory
        int flags = 1; // Survival inventory flag
        flags |= (1 << 1); // Client requested flag
        // Send full inventory data
        InventorySyncHandler.sendPacket(spectator, target);

        ServerSyncController.broadcastPacketToSpectators(target, new ClientboundScreenSyncPacket(target.getUUID(), flags));

    }

    private static void handle(ServerboundOpenedInventorySyncPacket packet, ServerPlayNetworking.Context ctx) {
        final var player = ctx.player();
        if (packet.isOpened()) {
            syncScreenOpened(player);
        } else {
            syncScreenClosed(player);
        }
    }

    /**
     * Called when a player opens their inventory or any container screen.
     * This method syncs the appropriate screen to all spectators who are viewing this player.
     *
     * @param target The player who opened their inventory/container
     */
    public static void syncScreenOpened(ServerPlayer target) {
        var spectators = ServerSyncController.getSpectators(target);
        if (spectators.isEmpty()) {
            return;
        }

        for (ServerPlayer spectator : spectators) {
            // Determine what type of screen the target player has open
            if (target.containerMenu != target.inventoryMenu) {
                // Player has a container open (chest, crafting table, furnace, etc.)
                ContainerSyncHandler.sendPacket(spectator, target);

            } else {
                // Player has their regular inventory open (survival/creative inventory)
                syncPlayerInventoryScreen(spectator, target);
            }
        }
    }

    /**
     * Called when a player closes their inventory or any container screen.
     * This method closes the synced screen for all spectators who are viewing this player.
     *
     * @param target The player who closed their inventory/container
     */
    public static void syncScreenClosed(ServerPlayer target) {
        var spectators = ServerSyncController.getSpectators(target);
        if (spectators.isEmpty()) {
            return;
        }

        for (ServerPlayer spectator : spectators) {
            ContainerSyncHandler.unsubscribeFromContainer(spectator, target);

            // Send screen sync packet to indicate inventory/container was closed
            int flags = 0; // No flags means close screen
            flags |= (1 << 3); // Screen closed flag
            ServerSyncController.broadcastPacketToSpectators(target, new ClientboundScreenSyncPacket(target.getUUID(), flags));
        }
    }

    public static void updatePlayerInventory(ServerPlayer player, ItemStack[] inventorySendSlots) {
        ServerSyncController.broadcastPacketToSpectators(player, new ClientboundInventorySyncPacket(player.getUUID(), inventorySendSlots));
    }

    /**
     * Syncs a player's regular inventory screen (survival/creative inventory) to a spectator.
     * This is used when the target player has opened their own inventory.
     *
     * @param spectator The spectator to sync the screen to
     * @param target The player whose inventory screen should be synced
     */
    private static void syncPlayerInventoryScreen(ServerPlayer spectator, ServerPlayer target) {
        // Send screen sync packet for player inventory (survival inventory)
        int flags = 1; // Survival inventory flag
        // Send the target's inventory data to the spectator
        InventorySyncHandler.sendPacket(spectator, target);

        ServerSyncController.broadcastPacketToSpectators(target, new ClientboundScreenSyncPacket(target.getUUID(), flags));
    }
}

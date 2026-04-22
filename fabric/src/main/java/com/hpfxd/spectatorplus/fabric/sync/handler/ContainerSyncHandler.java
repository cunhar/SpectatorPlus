package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundContainerSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContainerSyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerSyncHandler.class);

    // Map to track container listeners for each spectator
    // Key: spectator UUID, Value: listener instance
    private static final Map<UUID, ContainerListener> containerListeners = new HashMap<>();

    public static void init() {
        // Clean up listeners when a player disconnects.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            containerListeners.remove(handler.getPlayer().getUUID());
        });
    }

    /**
     * Unsubscribe a spectator from a target player's container.
     * This removes the container listener to prevent memory leaks and stale updates.
     */
    public static void unsubscribeFromContainer(ServerPlayer spectator, ServerPlayer target) {
        ContainerListener oldListener = containerListeners.remove(spectator.getUUID());
        if (oldListener != null) {
            target.containerMenu.removeSlotListener(oldListener);
        }
    }

    public static void sendPacket(ServerPlayer spectator, ServerPlayer target) {
        InventorySyncHandler.sendPacket(spectator, target);
        var containerMenu = target.containerMenu;
        if (containerMenu == target.inventoryMenu) {
            return; // No container open, just player inventory
        }
        ItemStack[] containerItems = extractContainerItems(containerMenu);
        ServerSyncController.broadcastPacketToSpectators(target, new ClientboundContainerSyncPacket(
            target.getUUID(),
            containerMenu.getType(),
            containerItems.length,
            containerItems
        ));

        int flags = 0; // Container screen (not survival inventory)
        flags |= (1 << 2); // Has dummy slots flag for container screens
        ServerSyncController.broadcastPacketToSpectators(target, new ClientboundScreenSyncPacket(target.getUUID(), flags));

        containerMenu.addSlotListener(createContainerListener(spectator, target, containerListeners));

        spectator.connection.send(new ClientboundOpenScreenPacket(
                containerMenu.containerId,
                containerMenu.getType(),
                target.getDisplayName()
        ));
    }

    private static ItemStack[] extractContainerItems(AbstractContainerMenu menu) {
        int containerSize = menu.slots.size() - 36; // Subtract player inventory slots
        ItemStack[] containerItems = new ItemStack[containerSize];

        for (int i = 0; i < containerSize; i++) {
            var slot = menu.getSlot(i);
            containerItems[i] = slot.getItem().copy();
        }

        return containerItems;
    }

    public static ContainerListener createContainerListener(ServerPlayer spectator, ServerPlayer target, Map<UUID, ContainerListener> listenerMap) {
        ContainerListener listener = new ContainerListener() {
            @Override
            public void slotChanged(@NotNull AbstractContainerMenu menu, int slotIndex, @NotNull ItemStack stack) {
                // Only sync container slots, not player inventory slots
                if (slotIndex < menu.slots.size() - 36 && ServerSyncController.getSpectators(target).contains(spectator)) {
                    ItemStack[] update = new ItemStack[menu.slots.size() - 36];
                    for (int i = 0; i < update.length; i++) {
                        if (i == slotIndex) {
                            update[i] = stack.copy();
                        } else {
                            // For efficiency, only send changed slot
                            update[i] = null;
                        }
                    }
                    ServerSyncController.sendPacket(spectator, new ClientboundContainerSyncPacket(
                        target.getUUID(),
                        menu.getType(),
                        update.length,
                        update
                    ));
                }
            }

            @Override
            public void dataChanged(@NotNull AbstractContainerMenu menu, int dataSlot, int value) {
                // Handle furnace progress, brewing stand progress, etc.
                // For now, we don't need to sync this separately
            }
        };
        listenerMap.put(spectator.getUUID(), listener);
        return listener;
    }
}

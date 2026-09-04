package com.hpfxd.spectatorplus.fabric.client.sync.screen;

import com.hpfxd.spectatorplus.fabric.client.gui.screens.SyncedInventoryScreen;
import com.hpfxd.spectatorplus.fabric.client.mixin.InventoryAccessor;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenCursorSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.setSyncData;
import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.syncData;

public class ScreenSyncController {
    public static boolean isPendingOpen = false;

    public static int syncedWindowId = Integer.MIN_VALUE;
    public static Inventory syncedInventory;
    public static Screen syncedScreen;

    public static void init() {
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof final Player player && syncData != null && syncData.playerId.equals(player.getUUID())) {
                closeSyncedInventory();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundScreenSyncPacket.TYPE, ScreenSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundInventorySyncPacket.TYPE, ScreenSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundScreenCursorSyncPacket.TYPE, ScreenSyncController::handle);
    }

    private static void handle(ClientboundScreenSyncPacket packet, ClientPlayNetworking.Context context) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        isPendingOpen = true;
        syncData.screen.isSurvivalInventory = packet.isSurvivalInventory();
        syncData.screen.isClientRequested = packet.isClientRequested();
        syncData.screen.hasDummySlots = packet.hasDummySlots();
    }

    private static void handle(ClientboundInventorySyncPacket packet, ClientPlayNetworking.Context context) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        if (syncData.screen.inventoryItems == null || syncData.screen.inventoryItems.size() != ClientboundInventorySyncPacket.ITEMS_LENGTH) {
            syncData.screen.inventoryItems = NonNullList.withSize(ClientboundInventorySyncPacket.ITEMS_LENGTH, ItemStack.EMPTY);
        }

        final ItemStack[] items = packet.items();
        for (int slot = 0; slot < items.length; slot++) {
            final ItemStack item = items[slot];
            if (item != null) {
                syncData.screen.inventoryItems.set(slot, item);
                if (syncedInventory != null) {
                    syncedInventory.setItem(slot, item);
                }
            }
        }
        // Also update global armorItems for convenience
        if (syncData.armorItems != null && items.length >= 40) {
            for (int slot = 36; slot < 40; slot++) {
                final ItemStack item = items[slot];
                if (item != null) {
                    syncData.armorItems.set(slot - 36, item);
                }
            }
        }
    }

    private static void handle(ClientboundScreenCursorSyncPacket packet, ClientPlayNetworking.Context context) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        syncData.screen.cursorItem = packet.cursor();
        syncData.screen.cursorItemSlot = packet.originSlot();
    }

    public static void reset() {
        isPendingOpen = false;
        syncedWindowId = Integer.MIN_VALUE;
        syncedInventory = null;
        syncedScreen = null;
    }

    public static void closeSyncedInventory() {
        if (syncedScreen != null) {
            syncedScreen.onClose();
        }
        reset();
    }

    public static void openPlayerInventory(Minecraft mc) {
        final Player player = SpecUtil.getCameraPlayer(mc);
        if (player == null) {
            return;
        }
        final SyncedInventoryScreen screen = new SyncedInventoryScreen(player);

        handleNewSyncedScreen(mc, screen);
    }

    public static <S extends Screen & MenuAccess<?>> void handleNewSyncedScreen(Minecraft mc, S screen) {
        if (mc == null || mc.player == null) {
            return;
        }
        isPendingOpen = false;
        mc.player.containerMenu = screen.getMenu();
        mc.gui.setScreen(screen);

        if (mc.gui.screen() != screen) {
            reset();
            if (syncData != null) {
                syncData.screen = null;
            }
            return;
        }

        syncedScreen = screen;

        ScreenEvents.remove(screen).register(s -> {
            reset();
            if (syncData != null) {
                syncData.screen = null;
            }
        });
    }

    public static boolean createInventory(Player spectated) {
        if (syncData.screen.inventoryItems == null) {
            return false;
        }

        EntityEquipment equipment = ((InventoryAccessor)spectated.getInventory()).getEquipment();
        syncedInventory = new Inventory(spectated, equipment);

        // Set all inventory slots (0-39)
        for (int i = 0; i < syncData.screen.inventoryItems.size(); i++) {
            syncedInventory.setItem(i, syncData.screen.inventoryItems.get(i));
        }
        // No direct access to armor list; setting slots 36-39 is sufficient for GUI
        return true;
    }
}

package com.hpfxd.spectatorplus.fabric.client.sync;

import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.EffectUtil;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundEffectsSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ClientSyncController {
    public static ClientSyncData syncData;
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundExperienceSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundFoodSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundHotbarSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSelectedSlotSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundEffectsSyncPacket.TYPE, ClientSyncController::handle);
        ClientLoginConnectionEvents.INIT.register((handler, client) -> createSyncDataIfNull(null));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> createSyncDataIfNull(null));

        ScreenSyncController.init();
    }

    private static void handle(ClientboundEffectsSyncPacket packet, ClientPlayNetworking.Context context) {
        createSyncDataIfNull(packet.playerId());
        syncData.effects = packet.effects();
        EffectUtil.updateEffectInstances(packet.effects());
    }

    private static void handle(ClientboundExperienceSyncPacket packet, ClientPlayNetworking.Context context) {
        createSyncDataIfNull(packet.playerId());

        var player = minecraft.player;
        if (player != null && (packet.progress() != player.experienceProgress || packet.level() != player.experienceLevel))
            player.experienceDisplayStartTick = player.tickCount;

        syncData.experienceProgress = packet.progress();
        syncData.experienceLevel = packet.level();
        syncData.experienceNeededForNextLevel = packet.neededForNextLevel();
    }

    private static void handle(ClientboundFoodSyncPacket packet, ClientPlayNetworking.Context context) {
        createSyncDataIfNull(packet.playerId());

        if (syncData.foodData == null) {
            syncData.foodData = new FoodData();
        }
        syncData.foodData.setFoodLevel(packet.food());
        syncData.foodData.setSaturation(packet.saturation());
    }

    private static void handle(ClientboundHotbarSyncPacket packet, ClientPlayNetworking.Context context) {
        createSyncDataIfNull(packet.playerId());

        final ItemStack[] items = packet.items();
        for (int slot = 0; slot < items.length; slot++) {
            final ItemStack item = items[slot];

            if (item != null) {
                syncData.hotbarItems.set(slot, item);
            }
        }
    }

    private static void handle(ClientboundSelectedSlotSyncPacket packet, ClientPlayNetworking.Context context) {
        createSyncDataIfNull(packet.playerId());

        syncData.selectedHotbarSlot = packet.selectedSlot();
    }

    public static void createSyncDataIfNull(UUID playerId) {
        if (playerId == null) {
            syncData = null;
        } else if (syncData == null || !syncData.playerId.equals(playerId)) {
            syncData = new ClientSyncData(playerId);
        }
    }
}

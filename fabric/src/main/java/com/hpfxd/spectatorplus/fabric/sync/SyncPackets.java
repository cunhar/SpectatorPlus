package com.hpfxd.spectatorplus.fabric.sync;

import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundContainerSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundEffectsSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenCursorSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class SyncPackets {
    public static void registerAll() {
        PayloadTypeRegistry.serverboundPlay().register(ServerboundOpenedInventorySyncPacket.TYPE, ServerboundOpenedInventorySyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundRequestInventoryOpenPacket.TYPE, ServerboundRequestInventoryOpenPacket.STREAM_CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ClientboundContainerSyncPacket.TYPE, ClientboundContainerSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundExperienceSyncPacket.TYPE, ClientboundExperienceSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundFoodSyncPacket.TYPE, ClientboundFoodSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundHotbarSyncPacket.TYPE, ClientboundHotbarSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundInventorySyncPacket.TYPE, ClientboundInventorySyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundScreenCursorSyncPacket.TYPE, ClientboundScreenCursorSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundScreenSyncPacket.TYPE, ClientboundScreenSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundSelectedSlotSyncPacket.TYPE, ClientboundSelectedSlotSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundEffectsSyncPacket.TYPE, ClientboundEffectsSyncPacket.STREAM_CODEC);
    }

    private SyncPackets() {
    }
}

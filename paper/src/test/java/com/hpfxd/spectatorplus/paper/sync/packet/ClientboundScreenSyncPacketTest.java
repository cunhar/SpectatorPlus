package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientboundScreenSyncPacketTest {

    @Test
    void testScreenSyncPacketFactoryFlags() {
        UUID playerId = UUID.randomUUID();

        ClientboundScreenSyncPacket none = ClientboundScreenSyncPacket.of(playerId, false, false, false);
        assertEquals(0, none.flags());

        ClientboundScreenSyncPacket survivalOnly = ClientboundScreenSyncPacket.of(playerId, true, false, false);
        assertEquals(0x01, survivalOnly.flags());

        ClientboundScreenSyncPacket clientRequestedOnly = ClientboundScreenSyncPacket.of(playerId, false, true, false);
        assertEquals(0x02, clientRequestedOnly.flags());

        ClientboundScreenSyncPacket dummySlotsOnly = ClientboundScreenSyncPacket.of(playerId, false, false, true);
        assertEquals(0x04, dummySlotsOnly.flags());

        ClientboundScreenSyncPacket all = ClientboundScreenSyncPacket.of(playerId, true, true, true);
        assertEquals(0x01 | 0x02 | 0x04, all.flags());
    }

    @Test
    void testScreenSyncPacketWriteAndRead() {
        UUID playerId = UUID.randomUUID();
        ClientboundScreenSyncPacket packet = ClientboundScreenSyncPacket.of(playerId, true, false, true);

        assertEquals("spectatorplus:screen_sync", packet.channel().asString());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        packet.write(out);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        UUID readPlayerId = SerializationUtil.readUuid(in);
        byte readFlags = in.readByte();

        assertEquals(playerId, readPlayerId);
        assertEquals(0x05, readFlags);
    }
}

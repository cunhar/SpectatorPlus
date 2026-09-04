package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientboundExperienceSyncPacketTest {

    @Test
    void testExperienceSyncPacketWriteAndRead() {
        UUID playerId = UUID.randomUUID();
        float progress = 0.65f;
        int neededForNextLevel = 112;
        int level = 30;

        ClientboundExperienceSyncPacket packet = new ClientboundExperienceSyncPacket(playerId, progress, neededForNextLevel, level);

        assertEquals(playerId, packet.playerId());
        assertEquals(progress, packet.progress());
        assertEquals(neededForNextLevel, packet.neededForNextLevel());
        assertEquals(level, packet.level());
        assertEquals("spectatorplus:experience_sync", packet.channel().asString());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        packet.write(out);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        UUID readPlayerId = SerializationUtil.readUuid(in);
        float readProgress = in.readFloat();
        int readNeededForNextLevel = in.readInt();
        int readLevel = in.readInt();

        assertEquals(playerId, readPlayerId);
        assertEquals(progress, readProgress);
        assertEquals(neededForNextLevel, readNeededForNextLevel);
        assertEquals(level, readLevel);
    }
}

package com.hpfxd.spectatorplus.paper.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerializationUtilTest {

    @Test
    void testUuidSerialization() {
        UUID uuid = UUID.randomUUID();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        SerializationUtil.writeUuid(out, uuid);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        UUID result = SerializationUtil.readUuid(in);

        assertEquals(uuid, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 127, 128, 255, 256, 16383, 16384, 2097151, Integer.MAX_VALUE, -1, -128, -2147483648})
    void testVarIntSerialization(int value) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        SerializationUtil.writeVarInt(out, value);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        int result = SerializationUtil.readVarInt(in);

        assertEquals(value, result);
    }

    @Test
    void testStringSerialization() {
        String testString = "Hello, SpectatorPlus! 🎮 Unicode: 測試 / テスト";
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        SerializationUtil.writeString(out, testString);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        String result = SerializationUtil.readString(in);

        assertEquals(testString, result);
    }

    @Test
    void testEmptyStringSerialization() {
        String testString = "";
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        SerializationUtil.writeString(out, testString);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        String result = SerializationUtil.readString(in);

        assertEquals(testString, result);
    }

    @Test
    void testNegativeStringLengthThrows() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        SerializationUtil.writeVarInt(out, -1);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        assertThrows(IllegalArgumentException.class, () -> SerializationUtil.readString(in));
    }

    @Test
    void testTooLargeStringLengthThrows() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        SerializationUtil.writeVarInt(out, 32768);

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        assertThrows(IllegalArgumentException.class, () -> SerializationUtil.readString(in));
    }

    @Test
    void testVarIntTooBigThrows() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        // 5 bytes with the continuation bit (0x80) set means 6th byte exceeds 35 bits
        for (int i = 0; i < 6; i++) {
            out.writeByte(0x80);
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        assertThrows(RuntimeException.class, () -> SerializationUtil.readVarInt(in));
    }
}

package com.hpfxd.spectatorplus.fabric.sync;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomPacketCodecsTest {

    @BeforeAll
    static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private RegistryFriendlyByteBuf createBuf() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }

    @Test
    void testReadItemZeroLengthReturnsEmpty() {
        RegistryFriendlyByteBuf buf = createBuf();
        buf.writeInt(0);

        ItemStack item = CustomPacketCodecs.readItem(buf);
        assertTrue(item.isEmpty());
    }

    @Test
    void testReadItemNegativeLengthThrowsDecoderException() {
        RegistryFriendlyByteBuf buf = createBuf();
        buf.writeInt(-1);

        assertThrows(DecoderException.class, () -> CustomPacketCodecs.readItem(buf));
    }

    @Test
    void testReadItemOversizedLengthThrowsDecoderException() {
        RegistryFriendlyByteBuf buf = createBuf();
        buf.writeInt(CustomPacketCodecs.MAX_ITEM_BYTES + 1);

        assertThrows(DecoderException.class, () -> CustomPacketCodecs.readItem(buf));
    }

    @Test
    void testReadItemsArrayOutOfBoundsThrows() {
        RegistryFriendlyByteBuf buf = createBuf();
        buf.writeInt(129); // MAX_ITEMS is 128

        assertThrows(DecoderException.class, () -> CustomPacketCodecs.readItems(buf));

        RegistryFriendlyByteBuf negBuf = createBuf();
        negBuf.writeInt(-1);
        assertThrows(DecoderException.class, () -> CustomPacketCodecs.readItems(negBuf));
    }
}

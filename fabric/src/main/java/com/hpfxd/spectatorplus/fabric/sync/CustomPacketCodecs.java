package com.hpfxd.spectatorplus.fabric.sync;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CustomPacketCodecs {
    /**
     * Maximum number of items allowed in a single items sync packet.
     * Vanilla player inventory has 41 slots; 128 provides generous headroom
     * while preventing malicious packets from causing OutOfMemoryError.
     */
    private static final int MAX_ITEMS = 128;

    private CustomPacketCodecs() {
    }

    public static ItemStack[] readItems(RegistryFriendlyByteBuf buf) {
        final int len = buf.readInt();
        if (len < 0 || len > MAX_ITEMS) {
            throw new DecoderException("Item array length " + len + " is outside allowed range [0, " + MAX_ITEMS + "]");
        }
        final ItemStack[] items = new ItemStack[len];

        for (int slot = 0; slot < len; slot++) {
            items[slot] = buf.readBoolean() ? CustomPacketCodecs.readItem(buf) : null;
        }

        return items;
    }

    public static void writeItems(RegistryFriendlyByteBuf buf, ItemStack[] items) {
        buf.writeInt(items.length);

        for (final ItemStack item : items) {
            buf.writeBoolean(item != null);
            if (item != null) {
                CustomPacketCodecs.writeItem(buf, item);
            }
        }
    }

    public static ItemStack readItem(RegistryFriendlyByteBuf buf) {
        try {
            int length = buf.readInt();
            if (length == 0) {
                return ItemStack.EMPTY;
            }
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
            net.minecraft.nbt.CompoundTag tag = net.minecraft.nbt.NbtIo.readCompressed(in, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            return ItemStack.OPTIONAL_CODEC.parse(buf.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).getOrThrow(DecoderException::new);
        } catch (Exception e) {
            throw new DecoderException("Failed to read ItemStack", e);
        }
    }

    public static void writeItem(RegistryFriendlyByteBuf buf, @NotNull ItemStack item) {
        try {
            if (item.isEmpty()) {
                buf.writeInt(0);
                return;
            }
            net.minecraft.nbt.Tag tag = ItemStack.OPTIONAL_CODEC.encodeStart(buf.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), item).getOrThrow(EncoderException::new);
            if (!(tag instanceof net.minecraft.nbt.CompoundTag)) {
                buf.writeInt(0);
                return;
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            net.minecraft.nbt.NbtIo.writeCompressed((net.minecraft.nbt.CompoundTag)tag, out);
            byte[] bytes = out.toByteArray();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (Exception e) {
            throw new EncoderException("Failed to write ItemStack", e);
        }
    }
}
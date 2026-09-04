package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.config.ClientConfig;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class SpectatorArmorHudRenderer {
    private static final Identifier EMPTY_ARMOR_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_ARMOR_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");

    private static final Identifier[] TEXTURE_EMPTY_SLOTS = new Identifier[] {
            EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET
    };

    public static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static final int ITEM_WIDTH = 16;
    public static final int ITEM_HEIGHT = 16;
    public static final int SPACING = 1;
    public static final int TOTAL_HEIGHT = SLOTS.length * ITEM_HEIGHT + (SLOTS.length - 1) * SPACING;
    public static final int PADDING = 4;

    public static boolean shouldRender() {
        return SpectatorClientMod.config.renderArmor && ClientSyncController.syncData != null
                && ClientSyncController.syncData.armorItems != null;
    }

    public static int getScreenTopY(Minecraft minecraft) {
        float scale = SpectatorClientMod.config.getArmorScale();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        boolean isUp = SpectatorClientMod.config.hudDirection == ClientConfig.HudDirection.UP;
        int totalScaledHeight = (int) (TOTAL_HEIGHT * scale);
        return isUp
                ? (screenHeight - PADDING - SpectatorClientMod.config.armorYOffset - totalScaledHeight)
                : (PADDING + SpectatorClientMod.config.armorYOffset);
    }

    public static int getScreenHeight(Minecraft minecraft) {
        float scale = SpectatorClientMod.config.getArmorScale();
        return (int) (TOTAL_HEIGHT * scale);
    }

    public static void render(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!shouldRender()) {
            return;
        }

        float scale = SpectatorClientMod.config.getArmorScale();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int virtualWidth = (int) (screenWidth / scale);
        int baseX = virtualWidth - ITEM_WIDTH - PADDING + (int) (SpectatorClientMod.config.armorXOffset / scale);

        boolean isUp = SpectatorClientMod.config.hudDirection == ClientConfig.HudDirection.UP;
        int armorBaseY = isUp
                ? (int) ((screenHeight - PADDING - SpectatorClientMod.config.armorYOffset) / scale) - TOTAL_HEIGHT
                : (int) ((PADDING + SpectatorClientMod.config.armorYOffset) / scale);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);

        for (int i = 0; i < SLOTS.length; i++) {
            int idx = ClientSyncController.syncData.armorItems.size() - 1 - i;
            ItemStack armorStack = idx >= 0 && idx < ClientSyncController.syncData.armorItems.size()
                    ? ClientSyncController.syncData.armorItems.get(idx)
                    : ItemStack.EMPTY;
            int y = armorBaseY + i * (ITEM_HEIGHT + SPACING);
            boolean isAir = armorStack == null || armorStack.isEmpty()
                    || armorStack.getItem() == net.minecraft.world.item.Items.AIR;

            if (isAir) {
                // Show empty slot icon if no item
                if (idx >= 0 && idx < TEXTURE_EMPTY_SLOTS.length) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE_EMPTY_SLOTS[idx], baseX, y,
                            ITEM_WIDTH, ITEM_HEIGHT);
                }
            } else {
                // Show item icon if present
                guiGraphics.item(armorStack, baseX, y);

                // Draw horizontal durability bar across the bottom of the armor icon
                if (armorStack.isDamageableItem() && armorStack.getMaxDamage() > 0) {
                    int durability = armorStack.getMaxDamage() - armorStack.getDamageValue();
                    float percent = (float) durability / armorStack.getMaxDamage();
                    int maxBarWidth = 14;
                    int barWidth = Math.min(maxBarWidth, Math.max(1, Math.round(maxBarWidth * percent)));
                    int barX = baseX + 1;
                    int barY = y + ITEM_HEIGHT - 2;
                    int barColor;
                    if (percent > 0.20F) {
                        barColor = 0xFF00FF00; // green
                    } else if (percent > 0.05F) {
                        barColor = 0xFFFFA500; // orange
                    } else {
                        barColor = 0xFFFF0000; // red
                    }

                    // Black background border
                    guiGraphics.fill(barX - 1, barY - 1, barX + maxBarWidth + 1, barY + 1, 0xFF000000);
                    // Colored durability bar across bottom
                    guiGraphics.fill(barX, barY, barX + barWidth, barY + 1, barColor);
                }
            }
        }

        guiGraphics.pose().popMatrix();
    }
}

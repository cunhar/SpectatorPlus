package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.config.ClientConfig;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.SyncedEffect;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SpectatorEffectsHudRenderer {
    private static final Identifier EFFECT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/effect_background");

    public static final int ITEM_WIDTH = 16;
    public static final int ITEM_HEIGHT = 16;
    public static final int SPACING = 1;
    public static final int PADDING = 4;

    public static List<SyncedEffect> getEffectsToRender(Minecraft minecraft) {
        if (!SpectatorClientMod.config.renderEffects) {
            return Collections.emptyList();
        }

        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(minecraft);
        if (spectated != null) {
            if (ClientSyncController.syncData != null && ClientSyncController.syncData.effects != null) {
                return ClientSyncController.syncData.effects;
            }
            return Collections.emptyList();
        }

        if (minecraft.player != null) {
            var activeEffects = minecraft.player.getActiveEffects();
            if (activeEffects.isEmpty()) {
                return Collections.emptyList();
            }

            List<SyncedEffect> list = new ArrayList<>(activeEffects.size());
            for (MobEffectInstance inst : activeEffects) {
                String effectKey = BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect().value()).toString();
                list.add(new SyncedEffect(effectKey, inst.getAmplifier(), inst.getDuration()));
            }
            return list;
        }

        return Collections.emptyList();
    }

    public static boolean shouldRender(Minecraft minecraft) {
        if (!SpectatorClientMod.config.renderEffects) {
            return false;
        }

        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(minecraft);
        if (spectated != null) {
            return ClientSyncController.syncData != null && ClientSyncController.syncData.effects != null
                    && !ClientSyncController.syncData.effects.isEmpty();
        }

        return minecraft.player != null && !minecraft.player.getActiveEffects().isEmpty();
    }

    public static void render(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!SpectatorClientMod.config.renderEffects) {
            return;
        }

        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(minecraft);
        final List<SyncedEffect> syncedEffects = (spectated != null && ClientSyncController.syncData != null)
                ? ClientSyncController.syncData.effects
                : null;
        final Collection<MobEffectInstance> localEffects = (spectated == null && minecraft.player != null)
                ? minecraft.player.getActiveEffects()
                : null;

        int totalEffects = (syncedEffects != null) ? syncedEffects.size() : ((localEffects != null) ? localEffects.size() : 0);
        if (totalEffects == 0) {
            return;
        }

        float scale = SpectatorClientMod.config.getEffectsScale();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int virtualWidth = (int) (screenWidth / scale);
        int baseX = virtualWidth - ITEM_WIDTH - PADDING + (int) (SpectatorClientMod.config.effectsXOffset / scale);

        boolean isUp = SpectatorClientMod.config.hudDirection == ClientConfig.HudDirection.UP;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);

        int effectAnchorScreenY = screenHeight - PADDING - SpectatorClientMod.config.effectsYOffset;
        int effectAnchorVirtualY = (int) (effectAnchorScreenY / scale);
        int effectTopScreenY = PADDING + SpectatorClientMod.config.effectsYOffset;
        int effectTopVirtualY = (int) (effectTopScreenY / scale);

        int effectIndex = 0;
        if (syncedEffects != null) {
            for (SyncedEffect effect : syncedEffects) {
                int y = isUp
                        ? effectAnchorVirtualY - ITEM_HEIGHT - effectIndex * (ITEM_HEIGHT + SPACING)
                        : effectTopVirtualY + effectIndex * (ITEM_HEIGHT + SPACING);

                renderEffectSlot(minecraft, guiGraphics, effect.effectKey, effect.amplifier, effect.duration, baseX, y);
                effectIndex++;
            }
        } else if (localEffects != null) {
            for (MobEffectInstance inst : localEffects) {
                int y = isUp
                        ? effectAnchorVirtualY - ITEM_HEIGHT - effectIndex * (ITEM_HEIGHT + SPACING)
                        : effectTopVirtualY + effectIndex * (ITEM_HEIGHT + SPACING);

                String effectKey = BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect().value()).toString();
                renderEffectSlot(minecraft, guiGraphics, effectKey, inst.getAmplifier(), inst.getDuration(), baseX, y);
                effectIndex++;
            }
        }

        guiGraphics.pose().popMatrix();
    }

    private static void renderEffectSlot(Minecraft minecraft, GuiGraphicsExtractor guiGraphics,
            String effectKey, int amplifier, int duration, int baseX, int y) {
        // Draw vanilla effect background
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SPRITE, baseX, y,
                ITEM_WIDTH, ITEM_HEIGHT);

        Identifier effectIcon = getEffectIcon(effectKey);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, effectIcon, baseX + 2, y + 2,
                ITEM_WIDTH - 4, ITEM_HEIGHT - 4);

        // Draw effect level as a small white number on the top right of the icon
        int level = amplifier + 1;
        String levelText = String.valueOf(level);
        int levelTextWidth = minecraft.font.width(levelText);
        int levelTextX = baseX + ITEM_WIDTH - (int) (levelTextWidth * 0.4F) - 3;
        int levelTextY = y + 2;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.5F, 0.5F);
        guiGraphics.text(minecraft.font, levelText, (int) (levelTextX / 0.5F),
                (int) (levelTextY / 0.5F), 0xFFFFFFFF, true);
        guiGraphics.pose().popMatrix();

        // Draw duration bar (1px wide) to the left of the effect icon, color changes with percent
        int maxDuration = 3600;
        float percent = maxDuration > 0 ? (duration / (float) maxDuration) : 1.0F;
        int maxBarHeight = ITEM_HEIGHT - 2;
        int barHeight = Math.min(maxBarHeight, (int) (maxBarHeight * percent));
        int barX = baseX + 1;
        int barY = y + ITEM_HEIGHT - 1 - barHeight;
        int barColor;
        if (percent > 0.20F) {
            barColor = 0xFF00FF00; // green
        } else if (percent > 0.05F) {
            barColor = 0xFFFFA500; // orange
        } else {
            barColor = 0xFFFF0000; // red
        }
        if (barHeight > 0) {
            guiGraphics.fill(barX, barY, barX + 2, barY + barHeight, barColor);
        }
    }

    public static Identifier getEffectIcon(String effectKey) {
        String key = effectKey;
        int colonIdx = key.indexOf(":");
        if (colonIdx != -1) {
            key = key.substring(colonIdx + 1);
        }
        return Identifier.withDefaultNamespace("mob_effect/" + key.toLowerCase());
    }
}

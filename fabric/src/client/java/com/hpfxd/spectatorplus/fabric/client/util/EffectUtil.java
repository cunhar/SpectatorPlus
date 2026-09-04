package com.hpfxd.spectatorplus.fabric.client.util;


import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.sync.SyncedEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.*;

public class EffectUtil {
    private static final Map<Holder<MobEffect>, MobEffectInstance> activeEffects = new HashMap<>();

    public static void updateEffectInstances(List<SyncedEffect> effects) {
        // Collect new effects safely
        Set<Holder<MobEffect>> newEffects = new HashSet<>();

        for (SyncedEffect syncedEffect : effects) {
            Identifier id = Identifier.tryParse(syncedEffect.effectKey);
            if (id != null) {
                BuiltInRegistries.MOB_EFFECT.get(id).ifPresent(newEffects::add);
            }
        }

        // Remove effects that are no longer present
        activeEffects.entrySet().removeIf(entry -> !newEffects.contains(entry.getKey()));

        // Add new effects while maintaining existing instances
        for (SyncedEffect syncedEffect : effects) {
            Identifier id = Identifier.tryParse(syncedEffect.effectKey);
            if (id != null) {
                BuiltInRegistries.MOB_EFFECT.get(id).ifPresent(effect -> {
                    if (!activeEffects.containsKey(effect)) {
                        MobEffectInstance instance = new MobEffectInstance(effect, syncedEffect.duration,
                                syncedEffect.amplifier, false, true, true);
                        activeEffects.put(effect, instance);
                    }
                });
            }
        }
    }

    public static void clearActiveEffects() {
        activeEffects.clear();
    }

    public static boolean hasValidSyncData() {
        return ClientSyncController.syncData != null &&
               ClientSyncController.syncData.effects != null;
    }

    public static boolean shouldUseSpectatorData() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null &&
               SpecUtil.getCameraPlayer(mc) != null &&
               hasValidSyncData();
    }

    // 直接返回原版格式的activeEffects
    public static Map<Holder<MobEffect>, MobEffectInstance> getActiveEffectsMap() {
        return activeEffects;
    }
}
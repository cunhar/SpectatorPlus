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
        // collecting new effects
        Set<Holder<MobEffect>> newEffects = new HashSet<>();

        for (SyncedEffect syncedEffect : effects) {
            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(syncedEffect.effectKey))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown effect: " + syncedEffect.effectKey));
            newEffects.add(effect);
        }

        // remove effects that are no longer present
        activeEffects.entrySet().removeIf(entry -> !newEffects.contains(entry.getKey()));

        // add new effects (keep existing instances' BlendState)
        for (SyncedEffect syncedEffect : effects) {
            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(syncedEffect.effectKey))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown effect: " + syncedEffect.effectKey));

            if (!activeEffects.containsKey(effect)) {
                MobEffectInstance instance = new MobEffectInstance(effect, syncedEffect.duration,
                        syncedEffect.amplifier, false, true, true);
                activeEffects.put(effect, instance);
            }
        }
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

    public static Map<Holder<MobEffect>, MobEffectInstance> getActiveEffectsMap() {
        return activeEffects;
    }
}
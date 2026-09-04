package com.hpfxd.spectatorplus.fabric.util;

import com.hpfxd.spectatorplus.fabric.client.util.EffectUtil;
import com.hpfxd.spectatorplus.fabric.sync.SyncedEffect;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EffectUtilTest {

    @BeforeAll
    static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void reset() {
        EffectUtil.clearActiveEffects();
    }

    @Test
    void testValidEffectParsing() {
        List<SyncedEffect> effects = List.of(
                new SyncedEffect("minecraft:speed", 1, 200),
                new SyncedEffect("minecraft:regeneration", 0, 100)
        );

        EffectUtil.updateEffectInstances(effects);

        assertEquals(2, EffectUtil.getActiveEffectsMap().size());
    }

    @Test
    void testUnknownEffectDoesNotCrash() {
        List<SyncedEffect> effects = List.of(
                new SyncedEffect("unknown_mod:nonexistent_effect", 0, 100),
                new SyncedEffect("invalid effect key with spaces", 0, 100)
        );

        assertDoesNotThrow(() -> EffectUtil.updateEffectInstances(effects));
        assertEquals(0, EffectUtil.getActiveEffectsMap().size());
    }

    @Test
    void testClearActiveEffects() {
        List<SyncedEffect> effects = List.of(new SyncedEffect("minecraft:speed", 0, 100));
        EffectUtil.updateEffectInstances(effects);
        assertFalse(EffectUtil.getActiveEffectsMap().isEmpty());

        EffectUtil.clearActiveEffects();
        assertTrue(EffectUtil.getActiveEffectsMap().isEmpty());
    }
}

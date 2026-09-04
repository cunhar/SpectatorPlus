package com.hpfxd.spectatorplus.fabric.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HotbarBoundsLogicTest {

    private boolean isValidHotbarSlot(int slot) {
        return slot >= 0 && slot < 9;
    }

    @Test
    void testValidHotbarSlots() {
        for (int i = 0; i < 9; i++) {
            assertTrue(isValidHotbarSlot(i), "Slot " + i + " should be valid");
        }
    }

    @Test
    void testInvalidHotbarSlots() {
        assertFalse(isValidHotbarSlot(-1), "Slot -1 should be invalid");
        assertFalse(isValidHotbarSlot(-2), "Slot -2 should be invalid");
        assertFalse(isValidHotbarSlot(9), "Slot 9 should be invalid");
        assertFalse(isValidHotbarSlot(10), "Slot 10 should be invalid");
        assertFalse(isValidHotbarSlot(100), "Slot 100 should be invalid");
    }
}

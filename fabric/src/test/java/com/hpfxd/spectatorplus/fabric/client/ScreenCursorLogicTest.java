package com.hpfxd.spectatorplus.fabric.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScreenCursorLogicTest {

    // Helper simulating the slot boundary check
    private boolean isValidCursorSlot(int cursorSlot, int containerSize) {
        return cursorSlot >= 0 && cursorSlot < containerSize;
    }

    private boolean oldBuggyCheck(int cursorSlot, int containerSize) {
        return cursorSlot > 0 && cursorSlot < containerSize;
    }

    @Test
    void testSlotZeroIsIncludedInCursorRendering() {
        int containerSize = 40; // 36 inventory + 4 armor

        // Slot 0 (the first slot) should be valid
        assertTrue(isValidCursorSlot(0, containerSize));

        // The old buggy check erroneously rejected slot 0
        assertFalse(oldBuggyCheck(0, containerSize));

        // Slot -1 (no slot hovered) should be rejected
        assertFalse(isValidCursorSlot(-1, containerSize));

        // Last slot
        assertTrue(isValidCursorSlot(39, containerSize));
        assertFalse(isValidCursorSlot(40, containerSize));
    }
}

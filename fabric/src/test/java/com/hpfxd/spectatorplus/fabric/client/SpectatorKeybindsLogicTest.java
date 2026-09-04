package com.hpfxd.spectatorplus.fabric.client;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class SpectatorKeybindsLogicTest {

    @Test
    void testCircularPlayerShiftWrapping() {
        List<String> players = List.of("PlayerA", "PlayerB", "PlayerC");
        int size = players.size();

        // Forward wrapping
        int current = 2; // at PlayerC
        int next = Math.floorMod(current + 1, size);
        assertEquals(0, next);
        assertEquals("PlayerA", players.get(next));

        // Backward wrapping from 0
        current = 0;
        int prev = Math.floorMod(current - 1, size);
        assertEquals(2, prev);
        assertEquals("PlayerC", players.get(prev));
    }

    @Test
    void testIterablesFindWithDefaultValueDoesNotThrow() {
        List<String> items = List.of("alpha", "beta", "gamma");

        // 2-arg find throws NoSuchElementException when missing
        assertThrows(NoSuchElementException.class, () ->
                Iterables.find(items, item -> item.equals("delta"))
        );

        // 3-arg find with null default returns null safely without throwing
        String result = Iterables.find(items, item -> item.equals("delta"), null);
        assertNull(result);

        // 3-arg find when present returns the element
        String found = Iterables.find(items, item -> item.equals("beta"), null);
        assertEquals("beta", found);
    }
}

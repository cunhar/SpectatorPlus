package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundEffectsSyncPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;
import com.hpfxd.spectatorplus.paper.effect.SyncedEffect;

public class EffectsSyncHandler implements Listener {
    private static final String PERMISSION = "spectatorplus.sync.effects";

    private final SpectatorPlugin plugin;

    public EffectsSyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private List<SyncedEffect> getSyncedEffects(Player player) {
        List<PotionEffect> effects = List.copyOf(player.getActivePotionEffects());
        return effects.stream()
                .map(pe -> new SyncedEffect(
                        pe.getType().getKey().toString(),
                        pe.getAmplifier(),
                        pe.getDuration()))
                .collect(Collectors.toList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();
        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(PERMISSION)) {
            List<SyncedEffect> effects = getSyncedEffects(target);
            this.plugin.getSyncController().sendPacket(spectator,
                    new ClientboundEffectsSyncPacket(target.getUniqueId(), effects));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                List<SyncedEffect> effects = getSyncedEffects(player);
                this.plugin.getSyncController().broadcastPacketToSpectators(player, PERMISSION,
                        new ClientboundEffectsSyncPacket(player.getUniqueId(), effects));
            });
        }
    }
}

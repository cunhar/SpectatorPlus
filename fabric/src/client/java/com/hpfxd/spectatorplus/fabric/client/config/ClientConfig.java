package com.hpfxd.spectatorplus.fabric.client.config;

public class ClientConfig {
    public enum HudDirection {
        UP,
        DOWN
    }

    public boolean renderStatus = true;
    public boolean renderStatusIfNoHotbar = true;
    public boolean renderHotbar = true;
    public boolean renderArms = true;
    public boolean renderArmor = true;
    public boolean renderEffects = true;
    public HudDirection hudDirection = HudDirection.UP;
    public int armorXOffset = -7;
    public int armorYOffset = 40;
    public int effectsXOffset = -100;
    public int effectsYOffset = 40;
    public int armorScalePercent = 75;
    public int effectsScalePercent = 75;
    public boolean showMenuButton = true;

    public float getArmorScale() {
        return Math.max(0.1F, this.armorScalePercent / 100.0F);
    }

    public float getEffectsScale() {
        return Math.max(0.1F, this.effectsScalePercent / 100.0F);
    }
    public boolean showSpectators = true;
    public boolean highlightSpectators = true;
    public boolean showInvisibleEntities = true;
    public boolean teleportAutoSpectate = false;
    public boolean keybindsOpenMenu = true;
    public boolean openScreens = true;
    public boolean hideTooltipUntilMouseMove = false;
    public boolean screensNoOverride = false;
}

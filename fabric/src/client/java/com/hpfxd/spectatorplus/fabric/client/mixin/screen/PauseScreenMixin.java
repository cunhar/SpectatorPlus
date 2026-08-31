package com.hpfxd.spectatorplus.fabric.client.mixin.screen;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.config.ClothConfigIntegration;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    private static final Identifier SPECTATORPLUS_ICON = Identifier.fromNamespaceAndPath("spectatorplus", "icon");

    @Shadow
    private boolean showPauseMenu;

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void spectatorplus$addPauseMenuButton(CallbackInfo ci) {
        if (!SpectatorClientMod.config.showMenuButton || !this.showPauseMenu) {
            return;
        }

        // Find existing 20x20 icon buttons in the pause menu icon row
        List<AbstractWidget> iconRowWidgets = new ArrayList<>();
        int targetY = -1;

        for (var child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                if (widget.getWidth() == 20 && widget.getHeight() == 20) {
                    if (targetY == -1) {
                        targetY = widget.getY();
                    }
                    if (Math.abs(widget.getY() - targetY) <= 4) {
                        iconRowWidgets.add(widget);
                    }
                }
            }
        }

        iconRowWidgets.sort(Comparator.comparingInt(AbstractWidget::getX));

        SpriteIconButton spectatorPlusButton = SpriteIconButton.builder(
                Component.translatable("gui.spectatorplus.config.title"),
                btn -> {
                    if (this.minecraft != null) {
                        this.minecraft.gui.setScreen(ClothConfigIntegration.getConfigScreen(this));
                    }
                },
                true
        )
        .size(20, 20)
        .sprite(SPECTATORPLUS_ICON, 16, 16)
        .tooltip(Component.translatable("gui.spectatorplus.config.title"))
        .build();

        this.addRenderableWidget(spectatorPlusButton);
        iconRowWidgets.add(spectatorPlusButton);

        int totalButtons = iconRowWidgets.size();
        int totalWidth = totalButtons * 20 + (totalButtons - 1) * 4;
        int startX = this.width / 2 - totalWidth / 2;
        int rowY = targetY != -1 ? targetY : (this.height / 4 + 72 + 24);

        for (int i = 0; i < totalButtons; i++) {
            AbstractWidget widget = iconRowWidgets.get(i);
            widget.setPosition(startX + i * 24, rowY);
        }
    }
}

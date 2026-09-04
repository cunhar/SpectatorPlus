package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.config.ClientConfig;
import com.hpfxd.spectatorplus.fabric.client.gui.screens.SpectatorArmorHudRenderer;
import com.hpfxd.spectatorplus.fabric.client.gui.screens.SpectatorEffectsHudRenderer;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.SyncedEffect;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator.Context.Local;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Hud.class)
public abstract class GuiMixin {
    // Local copy of vanilla overlay resource location
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract SpectatorGui getSpectatorGui();

    @Shadow
    public abstract boolean isHidden();

    @Shadow
    private void extractItemHotbar(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {}

    @Inject(method = "extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
    private void spectatorplus$cancelRenderEffects(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker,
            CallbackInfo ci) {
        if (SpectatorClientMod.config.renderEffects) {
            ci.cancel();
            if (!this.isHidden() && !this.getSpectatorGui().isMenuActive()) {
                SpectatorEffectsHudRenderer.render(this.minecraft, guiGraphics, deltaTracker);
            }
        }
    }

    @Redirect(method = "extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean spectatorplus$renderScoping(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.isScoping();
        }
        return instance.isScoping();
    }

    @Redirect(method = "extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$renderItemCameraOverlay(LocalPlayer instance, EquipmentSlot slot) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getItemBySlot(slot);
        }
        return instance.getItemBySlot(slot);
    }

    @Redirect(method = "extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    private int spectatorplus$renderFreezeOverlay(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getTicksFrozen();
        }
        return instance.getTicksFrozen();
    }

    @Redirect(method = "extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getPercentFrozen()F"))
    private float spectatorplus$renderFreezeOverlayPercent(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getPercentFrozen();
        }
        return instance.getPercentFrozen();
    }

    @Shadow
    protected abstract void extractSelectedItemName(GuiGraphicsExtractor guiGraphicsExtractor);

    @Inject(method = "extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;extractHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void spectatorplus$renderHotbar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci,
            @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (!this.getSpectatorGui().isMenuActive() && !this.isHidden()) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            spectatedRef.set(spectated);

            if (spectated != null) {
                if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1
                        && !spectated.isSpectator() && SpectatorClientMod.config.renderHotbar) {
                    this.extractItemHotbar(guiGraphics, deltaTracker);
                    this.extractSelectedItemName(guiGraphics);
                }

                SpectatorArmorHudRenderer.render(this.minecraft, guiGraphics, deltaTracker);
            }
        }
    }

    @ModifyExpressionValue(method = "extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$renderHealth(boolean original,
            @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (original) {
            return true;
        }

        final AbstractClientPlayer spectated = spectatedRef.get();
        return spectated != null && !spectated.isCreative() && !spectated.isSpectator()
                && this.spectatorplus$isStatusEnabled();
    }

    @Redirect(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean spectatorplus$renderExperience(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return !spectated.isCreative() && !spectated.isSpectator() && ClientSyncController.syncData != null
                    && ClientSyncController.syncData.experienceLevel != -1 && this.spectatorplus$isStatusEnabled();
        }

        return instance.hasExperience();
    }

    @Redirect(method = "nextContextualInfoState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean spectatorplus$hasExperience(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return !spectated.isCreative() && !spectated.isSpectator() && ClientSyncController.syncData != null
                    && ClientSyncController.syncData.experienceLevel != -1 && this.spectatorplus$isStatusEnabled();
        }

        return instance.hasExperience();
    }

    @Unique
    private boolean spectatorplus$isStatusEnabled() {
        if (!SpectatorClientMod.config.renderStatus) {
            return false;
        }

        return SpectatorClientMod.config.renderStatusIfNoHotbar
                || (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1);
    }

    @Inject(method = "canRenderCrosshairForSpectator(Lnet/minecraft/world/phys/HitResult;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void spectatorplus$renderCrosshair(HitResult rayTrace, CallbackInfoReturnable<Boolean> cir) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            cir.setReturnValue(!spectated.isSpectator());
        }
    }

    @Redirect(method = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
    private float spectatorplus$fixCrosshairAttackStrength(LocalPlayer instance, float adjustTicks) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getAttackStrengthScale(adjustTicks);
        }
        return instance.getAttackStrengthScale(adjustTicks);
    }

    @Redirect(method = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getCurrentItemAttackStrengthDelay()F"))
    private float spectatorplus$fixCrosshairCurrentItemAttackStrengthDelay(LocalPlayer instance) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getCurrentItemAttackStrengthDelay();
        }
        return instance.getCurrentItemAttackStrengthDelay();
    }

    @Redirect(method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$moveHeldItemTooltipUp(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null && !spectated.isCreative() && !spectated.isSpectator()) {
            return true;
        }
        return instance.canHurtPlayer();
    }

    @ModifyConstant(method = "extractPlayerHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", constant = @Constant(intValue = 39))
    private int spectatorplus$moveHealthDown(int constant) {
        if ((ClientSyncController.syncData == null || ClientSyncController.syncData.selectedHotbarSlot == -1)
                && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            // hotbar sync data not present, shift health down
            return constant - 27;
        }
        return constant;
    }

    @WrapWithCondition(method = "extractPlayerHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"))
    private boolean spectatorplus$hideNonSyncedFood(Hud instance, GuiGraphicsExtractor guiGraphics, Player player, int y,
            int x) {
        return (ClientSyncController.syncData != null && ClientSyncController.syncData.foodData != null)
                || SpecUtil.getCameraPlayer(this.minecraft) == null;
    }

    @Redirect(method = "extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedSlot()I", opcode = Opcodes.GETFIELD))
    private int spectatorplus$showSyncedSelectedSlot(Inventory inventory) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1
                && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.selectedHotbarSlot;
        }
        return inventory.getSelectedSlot();
    }

    @Redirect(method = "extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;"))
    private FoodData spectatorplus$showSyncedFood(Player instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.foodData != null
                && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.foodData;
        }
        return instance.getFoodData();
    }

    @Redirect(method = "extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean spectatorplus$showSyncedFoodSprite(Player instance,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        final LocalPlayer player = this.minecraft.player;
        if (player != null && player.hasEffect(effect)) {
            return true;
        }
        return instance.hasEffect(effect);
    }

    @Redirect(method = "extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$showSyncedItems(Inventory instance, int slot) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1
                && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.hotbarItems.get(slot);
        }
        return instance.getItem(slot);
    }

    @Redirect(method = "extractHotbarAndDecorations", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int spectatorplus$showSyncedExperienceLevel(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1
                && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceLevel;
        }
        return instance.experienceLevel;
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$modifyTooltipTick(Inventory instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1
                && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.hotbarItems.get(ClientSyncController.syncData.selectedHotbarSlot);
        }
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getInventory().getSelectedItem();
        }
        return instance.getSelectedItem();
    }

    @ModifyExpressionValue(method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$moveHeldItemTooltipUp(boolean original) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null && !spectated.isCreative() && !spectated.isSpectator()) {
            return true;
        }
        return original;
    }
}

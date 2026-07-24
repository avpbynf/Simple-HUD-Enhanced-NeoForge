package com.soradgaming.simplehudenhanced.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.hud.StatusEffectBarRenderer;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders the status-effect duration bars on the HUD.
 * <p>
 * On Fabric this injected into {@code InGameHud#renderStatusEffectOverlay}; the Mojmap equivalent is
 * {@code Gui#renderEffects}, hooked at the {@code MobEffectTextureManager#get} call so the same
 * effect instance and slot coordinates are in scope. The {@code i}/{@code j} slot position ints are
 * the third and fourth int locals (ordinals 2 and 3), matching the Fabric original.
 */
@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/MobEffectTextureManager;get(Lnet/minecraft/core/Holder;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", ordinal = 0)
    )
    private void onRenderStatusEffectOverlay(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci, @Local MobEffectInstance statusEffectInstance, @Local(ordinal = 2) int k, @Local(ordinal = 3) int l) {
        SimpleHudEnhancedConfig config = AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).getConfig();
        StatusEffectBarRenderer.render(context, statusEffectInstance, k, l, 24, 24, config);
        RenderSystem.enableBlend(); // disabled by GuiGraphics#fill
    }
}

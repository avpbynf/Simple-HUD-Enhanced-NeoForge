package com.soradgaming.simplehudenhanced.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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
 * {@code Gui#renderEffects}. The Fabric build hooked the {@code DrawContext#drawGuiTexture} call that
 * draws each effect icon; the 1.21.11 Mojmap analogue is the {@code GuiGraphics#blitSprite} overload
 * with a colour argument ({@code (RenderPipeline, Identifier, IIIII)}), which is only used for the
 * icon draw. The effect instance and slot position are captured from the surrounding locals: the
 * {@code k}/{@code l} slot ints are the third and fourth int locals (ordinals 2 and 3), matching the
 * Fabric original.
 */
@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderEffects(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V", ordinal = 0)
    )
    private void onRenderStatusEffectOverlay(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci, @Local MobEffectInstance statusEffectInstance, @Local(ordinal = 2) int k, @Local(ordinal = 3) int l) {
        SimpleHudEnhancedConfig config = AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).getConfig();
        StatusEffectBarRenderer.render(context, statusEffectInstance, k, l, 24, 24, config);
    }
}

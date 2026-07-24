package com.soradgaming.simplehudenhanced.mixin;

import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.hud.HUD;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class)
public class GameRender {
    @Unique
    private SimpleHudEnhancedConfig config;
    @Unique
    private boolean simpleHudEnhancedInitialized;

    @Unique
    private void initializeIfNeeded() {
        if (this.simpleHudEnhancedInitialized) {
            return;
        }

        this.config = AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).getConfig();
        AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).registerSaveListener((manager, data) -> {
            this.config = data;

            HUD hud = HUD.getInstance();
            if (hud != null) {
                hud.sprintTimer = data.paperDoll.paperDollTimeOut;
            }

            return InteractionResult.SUCCESS;
        });

        HUD.initialize(Minecraft.getInstance(), this.config);
        this.simpleHudEnhancedInitialized = true;
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    private void onClientTick(CallbackInfo ci) {
        initializeIfNeeded();

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        HUD hud = HUD.getInstance();
        if (hud != null) {
            hud.getEquipmentCache().updateCache(client.player);
            hud.getMovementCache().updateCache(client.player);
            hud.getStatusCache().updateCache();
        }
    }
}
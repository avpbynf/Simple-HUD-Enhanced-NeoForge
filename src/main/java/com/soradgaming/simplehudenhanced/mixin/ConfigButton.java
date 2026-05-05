package com.soradgaming.simplehudenhanced.mixin;

import com.soradgaming.simplehudenhanced.SimpleHudEnhanced;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.utli.LegacyTexturedButtonWidget;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen;

@Mixin(PauseScreen.class)
public class ConfigButton extends Screen {
    protected ConfigButton(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    private void onCreatePauseMenu(CallbackInfo ci) {
        if (SimpleHudEnhanced.isModMenuInstalled()) {
            return;
        }

        List<AbstractWidget> buttons = Screens.getWidgets(this);
        AbstractWidget anchor = simpleHudEnhanced$findPauseAnchor(buttons);

        int buttonX = anchor != null ? anchor.getX() + anchor.getWidth() + 2 : this.width / 2 + 4 + 100 + 2;
        int buttonY = anchor != null ? anchor.getY() : this.height / 4 + 72 - 16 + 1;

        // Use the same pause menu pass as vanilla and place this right of report/share.
        this.addRenderableWidget(
                new LegacyTexturedButtonWidget(
                        buttonX,
                        buttonY,
                        20,
                        20,
                        0,
                        0,
                        20,
                        Identifier.fromNamespaceAndPath("simplehudenhanced", "textures/mods_button.png"),
                        32,
                        64,
                        _ -> Minecraft.getInstance().setScreen(getConfigScreen(SimpleHudEnhancedConfig.class, this).get()),
                        Component.literal("Simple Hud Enhanced Config")
                )
        );
    }

    @Unique
    private static AbstractWidget simpleHudEnhanced$findPauseAnchor(List<AbstractWidget> buttons) {
        for (AbstractWidget widget : buttons) {
            if (simpleHudEnhanced$hasTranslationKey(widget.getMessage(), "menu.playerReporting")
                    || simpleHudEnhanced$hasTranslationKey(widget.getMessage(), "menu.shareToLan")) {
                return widget;
            }
        }
        return null;
    }

    @Unique
    private static boolean simpleHudEnhanced$hasTranslationKey(Component component, String key) {
        return component.getContents() instanceof TranslatableContents translatable
                && key.equals(translatable.getKey());
    }
}

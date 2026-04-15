package com.soradgaming.simplehudenhanced.mixin;

import com.soradgaming.simplehudenhanced.SimpleHudEnhanced;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.utli.LegacyTexturedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

import static me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen;

@Mixin(Minecraft.class)
public class ConfigButton {
    @Unique
    private Screen simpleHudEnhanced$lastScreen;

    @Unique
    private boolean simpleHudEnhanced$buttonAddedForScreen;

    @Inject(method = "handleInputEvents", at = @At("TAIL"))
    private void addCustomButton(CallbackInfo ci) {
        if (SimpleHudEnhanced.isModMenuInstalled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Screen screen = client.screen;
        if (!(screen instanceof TitleScreen)) {
            return;
        }

        if (screen != this.simpleHudEnhanced$lastScreen) {
            this.simpleHudEnhanced$lastScreen = screen;
            this.simpleHudEnhanced$buttonAddedForScreen = false;
        }

        if (this.simpleHudEnhanced$buttonAddedForScreen) {
            return;
        }

        LegacyTexturedButtonWidget button = new LegacyTexturedButtonWidget(
                screen.width / 2 + 4 + 100 + 2,
                screen.height / 4 + 72 - 16,
                20,
                20,
                0,
                0,
                20,
                Identifier.fromNamespaceAndPath("simplehudenhanced", "textures/mods_button.png"),
                32,
                64,
                ignored -> client.setScreen(getConfigScreen(SimpleHudEnhancedConfig.class, screen).get()),
                Component.empty()
        );

        this.simpleHudEnhanced$buttonAddedForScreen = simpleHudEnhanced$tryAddWidget(screen, button);
    }

    @Unique
    private static boolean simpleHudEnhanced$tryAddWidget(Screen screen, AbstractWidget widget) {
        return simpleHudEnhanced$tryInvoke(screen, "addRenderableWidget", AbstractWidget.class, widget)
                || simpleHudEnhanced$tryInvoke(screen, "addWidget", GuiEventListener.class, widget)
                || simpleHudEnhanced$tryInvoke(screen, "addDrawableChild", AbstractWidget.class, widget);
    }

    @Unique
    private static boolean simpleHudEnhanced$tryInvoke(Screen screen, String methodName, Class<?> parameterType, Object arg) {
        try {
            Method method = Screen.class.getDeclaredMethod(methodName, parameterType);
            method.setAccessible(true);
            method.invoke(screen, arg);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}

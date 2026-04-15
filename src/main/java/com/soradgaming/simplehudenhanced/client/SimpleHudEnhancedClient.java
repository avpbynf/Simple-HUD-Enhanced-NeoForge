package com.soradgaming.simplehudenhanced.client;

import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.hud.HUD;
import com.soradgaming.simplehudenhanced.utli.Utilities;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SimpleHudEnhancedClient implements ClientModInitializer {
    private ConfigHolder<SimpleHudEnhancedConfig> configHolder;
    private HUD hud;

    @Override
    public void onInitializeClient() {
        // Register the config holder for SimpleHudEnhancedConfig
        this.configHolder = AutoConfig.register(SimpleHudEnhancedConfig.class, Toml4jConfigSerializer::new);
        // Register the keybindings
        this.registerKeybindings();

        // Register a HUD element to render the custom HUD
        HudElementRegistry.attachElementBefore(VanillaHudElements.TITLE_AND_SUBTITLE , Identifier.fromNamespaceAndPath("simplehudenhanced", "hud"), (graphics, tickCounter) -> {
            if (this.hud == null) {
                this.hud = HUD.getInstance();
                // Render the HUD on next tick
            } else {
                this.hud.drawHud(graphics);
            }
        });
    }

    void registerKeybindings() {
        KeyMapping toggleHudKeybinding = new KeyMapping(
                "key.simplehudenhanced.toggle_hud",
                GLFW.GLFW_KEY_GRAVE_ACCENT, // ` key
                KeyMapping.Category.register(Identifier.parse("key.category.simplehudenhanced"))
        );

        // Initialize previousDebugHudState
        boolean[] previousDebugHudState = new boolean[]{false};

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Detect changes in shouldShowDebugHud state
            boolean currentDebugHudState = Minecraft.getInstance().getDebugOverlay().showDebugScreen();
            SimpleHudEnhancedConfig config = this.configHolder.getConfig();

            if (currentDebugHudState != previousDebugHudState[0]) {
                if (currentDebugHudState) {
                    config.uiConfig.toggleSimpleHUDEnhanced = false; // Disable HUD when debug HUD is shown
                } else if (!config.uiConfig.toggleSimpleHUDEnhanced) {
                    config.uiConfig.toggleSimpleHUDEnhanced = true; // Re-enable HUD when debug HUD is hidden
                }
                AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).save();
            }

            // Update the previous state for the next tick
            previousDebugHudState[0] = currentDebugHudState;

            if (toggleHudKeybinding.consumeClick()) {
                String chatMessage = "key.simplehudenhanced.toggle_hud.chat_message.on";
                if (config.uiConfig.toggleSimpleHUDEnhanced) {
                    chatMessage = "key.simplehudenhanced.toggle_hud.chat_message.off";
                }

                client.player.sendOverlayMessage(Utilities.translatable(chatMessage));
                config.uiConfig.toggleSimpleHUDEnhanced = !config.uiConfig.toggleSimpleHUDEnhanced;
                AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).save();
            }
        });
    }
}

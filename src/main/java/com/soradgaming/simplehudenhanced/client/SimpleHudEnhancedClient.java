package com.soradgaming.simplehudenhanced.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.soradgaming.simplehudenhanced.SimpleHudEnhanced;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.hud.HUD;
import com.soradgaming.simplehudenhanced.utli.LegacyTexturedButtonWidget;
import com.soradgaming.simplehudenhanced.utli.Utilities;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public class SimpleHudEnhancedClient {
    private ConfigHolder<SimpleHudEnhancedConfig> configHolder;
    private HUD hud;
    private KeyMapping toggleHudKeybinding;
    private final KeyMapping.Category hudCategory =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("simplehudenhanced", "hud"));
    private boolean previousDebugHudState = false;

    /**
     * Entry point called from the {@code @Mod} main class constructor (client only).
     */
    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        new SimpleHudEnhancedClient().setup(modEventBus, modContainer);
    }

    private void setup(IEventBus modEventBus, ModContainer modContainer) {
        // Register the config holder for SimpleHudEnhancedConfig
        this.configHolder = AutoConfig.register(SimpleHudEnhancedConfig.class, Toml4jConfigSerializer::new);

        // Expose the config screen through NeoForge's built-in mod-list "Config" button
        // (this replaces the Fabric ModMenu integration).
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, modListScreen) ->
                        AutoConfigClient.getConfigScreen(SimpleHudEnhancedConfig.class, modListScreen).get());

        // Mod-bus events: registration of key mappings and HUD layers.
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerGuiLayers);

        // Game-bus events: per-tick logic and pause-menu button injection.
        NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
        NeoForge.EVENT_BUS.addListener(this::onScreenInitPost);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        this.toggleHudKeybinding = new KeyMapping(
                "key.simplehudenhanced.toggle_hud",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                this.hudCategory
        );
        event.registerCategory(this.hudCategory);
        event.register(this.toggleHudKeybinding);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        // Attach the custom HUD just below the vanilla title/subtitle layer, mirroring the Fabric
        // HudElementRegistry.attachElementBefore(VanillaHudElements.TITLE_AND_SUBTITLE, ...) ordering.
        event.registerBelow(VanillaGuiLayers.TITLE,
                Identifier.fromNamespaceAndPath("simplehudenhanced", "hud"),
                (graphics, deltaTracker) -> {
                    if (this.hud == null) {
                        this.hud = HUD.getInstance();
                        // Render the HUD on next tick
                    } else {
                        this.hud.drawHud(graphics);
                    }
                });
    }

    private void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // Detect changes in shouldShowDebugHud state
        boolean currentDebugHudState = client.getDebugOverlay().showDebugScreen();
        SimpleHudEnhancedConfig config = this.configHolder.getConfig();

        if (currentDebugHudState != previousDebugHudState) {
            if (currentDebugHudState) {
                config.uiConfig.toggleSimpleHUDEnhanced = false; // Disable HUD when debug HUD is shown
            } else if (!config.uiConfig.toggleSimpleHUDEnhanced) {
                config.uiConfig.toggleSimpleHUDEnhanced = true; // Re-enable HUD when debug HUD is hidden
            }
            AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).save();
        }

        // Update the previous state for the next tick
        previousDebugHudState = currentDebugHudState;

        if (toggleHudKeybinding != null && toggleHudKeybinding.consumeClick()) {
            String chatMessage = "key.simplehudenhanced.toggle_hud.chat_message.on";
            if (config.uiConfig.toggleSimpleHUDEnhanced) {
                chatMessage = "key.simplehudenhanced.toggle_hud.chat_message.off";
            }

            client.player.sendOverlayMessage(Utilities.translatable(chatMessage));
            config.uiConfig.toggleSimpleHUDEnhanced = !config.uiConfig.toggleSimpleHUDEnhanced;
            AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).save();
        }
    }

    /**
     * Adds a config button to the pause menu when ModMenu is absent (always the case on NeoForge).
     * Replaces the Fabric {@code ConfigButton} mixin, which relied on {@code Screens.getWidgets}.
     */
    private void onScreenInitPost(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen pauseScreen)) {
            return;
        }
        if (SimpleHudEnhanced.isModMenuInstalled()) {
            return;
        }

        AbstractWidget anchor = findPauseAnchor(event);

        int buttonX = anchor != null ? anchor.getX() + anchor.getWidth() + 2 : pauseScreen.width / 2 + 4 + 100 + 2;
        int buttonY = anchor != null ? anchor.getY() : pauseScreen.height / 4 + 72 - 16 + 1;

        // Place this right of report/share, mirroring the previous mixin behaviour.
        event.addListener(
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
                        _ -> Minecraft.getInstance().setScreenAndShow(
                                AutoConfigClient.getConfigScreen(SimpleHudEnhancedConfig.class, pauseScreen).get()),
                        Component.literal("Simple Hud Enhanced Config")
                )
        );
    }

    private static AbstractWidget findPauseAnchor(ScreenEvent.Init event) {
        for (GuiEventListener listener : event.getListenersList()) {
            if (listener instanceof AbstractWidget widget
                    && (hasTranslationKey(widget.getMessage(), "menu.playerReporting")
                    || hasTranslationKey(widget.getMessage(), "menu.shareToLan"))) {
                return widget;
            }
        }
        return null;
    }

    private static boolean hasTranslationKey(Component component, String key) {
        return component.getContents() instanceof TranslatableContents translatable
                && key.equals(translatable.getKey());
    }
}

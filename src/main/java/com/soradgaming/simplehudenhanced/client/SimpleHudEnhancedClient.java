package com.soradgaming.simplehudenhanced.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.soradgaming.simplehudenhanced.SimpleHudEnhanced;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.hud.HUD;
import com.soradgaming.simplehudenhanced.utli.LegacyTexturedButtonWidget;
import com.soradgaming.simplehudenhanced.utli.Utilities;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
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
    private boolean initialised = false;

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
                        AutoConfig.getConfigScreen(SimpleHudEnhancedConfig.class, modListScreen).get());

        // Keep the paper-doll sprint timer in sync when the config is saved.
        this.configHolder.registerSaveListener((manager, data) -> {
            HUD instance = HUD.getInstance();
            if (instance != null) {
                instance.sprintTimer = data.paperDoll.paperDollTimeOut;
            }
            return InteractionResult.SUCCESS;
        });

        // Mod-bus events: registration of key mappings and HUD layers.
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerGuiLayers);

        // Game-bus events: per-tick logic and pause-menu button injection.
        NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
        NeoForge.EVENT_BUS.addListener(this::onScreenInitPost);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // NeoForge 1.21.1 still uses a String translation-key category (the KeyMapping.Category
        // object type only exists on newer versions), so keep the original lang key.
        this.toggleHudKeybinding = new KeyMapping(
                "key.simplehudenhanced.toggle_hud",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.category.simplehudenhanced.hud"
        );
        event.register(this.toggleHudKeybinding);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        // Attach the custom HUD just below the vanilla title/subtitle layer, mirroring the Fabric
        // build's InGameHud.render HEAD injection ordering. Modded layers registered through this
        // event are inserted raw: unlike vanilla layers they are NOT wrapped in the hideGui guard,
        // so F1 (hide GUI) and F3 (debug overlay) are checked explicitly here. This reproduces the
        // Fabric GameRender mixin, which skipped drawHud while the GUI was hidden or F3 was open.
        event.registerBelow(VanillaGuiLayers.TITLE,
                ResourceLocation.fromNamespaceAndPath("simplehudenhanced", "hud"),
                (graphics, deltaTracker) -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.options.hideGui || client.getDebugOverlay().showDebugScreen()) {
                        return;
                    }
                    HUD instance = HUD.getInstance();
                    if (instance != null) {
                        instance.drawHud(graphics);
                    }
                });
    }

    private void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();

        // Lazy initialisation: defer until the game loop is running (Minecraft is fully ready).
        if (!initialised) {
            HUD.initialize(client, this.configHolder.getConfig());
            this.hud = HUD.getInstance();
            initialised = true;
        }

        if (client.player == null) return;

        // Update the caches every client tick (the Fabric build used a background thread).
        if (this.hud != null) {
            this.hud.getEquipmentCache().updateCache(client.player);
            this.hud.getMovementCache().updateCache(client.player);
            this.hud.getStatusCache().updateCache();
        }

        SimpleHudEnhancedConfig config = this.configHolder.getConfig();

        if (toggleHudKeybinding != null && toggleHudKeybinding.consumeClick()) {
            String chatMessage = "key.simplehudenhanced.toggle_hud.chat_message.on";
            if (config.uiConfig.toggleSimpleHUDEnhanced) {
                chatMessage = "key.simplehudenhanced.toggle_hud.chat_message.off";
            }

            client.player.displayClientMessage(Utilities.translatable(chatMessage), true);
            config.uiConfig.toggleSimpleHUDEnhanced = !config.uiConfig.toggleSimpleHUDEnhanced;
            AutoConfig.getConfigHolder(SimpleHudEnhancedConfig.class).save();
        }
    }

    /**
     * Adds a config button to the pause menu when ModMenu is absent (always the case on NeoForge).
     * Replaces the Fabric {@code ConfigButton} mixin, which relied on {@code ScreenInvoker}.
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
                        ResourceLocation.fromNamespaceAndPath("simplehudenhanced", "textures/mods_button.png"),
                        32,
                        64,
                        button -> Minecraft.getInstance().setScreen(
                                AutoConfig.getConfigScreen(SimpleHudEnhancedConfig.class, pauseScreen).get()),
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

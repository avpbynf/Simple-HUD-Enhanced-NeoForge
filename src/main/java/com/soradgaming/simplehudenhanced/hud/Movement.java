package com.soradgaming.simplehudenhanced.hud;

import com.soradgaming.simplehudenhanced.cache.MovementCache;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.utli.Utilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;

public class Movement {
    private final Minecraft client;
    private final Font renderer;
    private final SimpleHudEnhancedConfig config;
    private final GuiGraphicsExtractor context;
    private final MovementCache movementCache;

    public Movement(GuiGraphicsExtractor context, SimpleHudEnhancedConfig config, MovementCache movementCache) {
        this.client = Minecraft.getInstance();
        this.renderer = client.font;
        this.config = config;
        this.context = context;
        this.movementCache = movementCache;
    }

    public void init(GameInfo GameInformation) {
        if (config.movementStatus.movementTypes.toggleSwimmingStatus && GameInformation.isPlayerSwimming()) {
            draw(context, "text.hud.simplehudenhanced.swimming");
        } else if (config.movementStatus.movementTypes.toggleFlyingStatus && GameInformation.isPlayerFlying()) {
            draw(context, "text.hud.simplehudenhanced.flying");
        } else if (config.movementStatus.movementTypes.toggleSneakStatus && GameInformation.isPlayerSneaking()) {
            draw(context, "text.hud.simplehudenhanced.sneaking");
        } else if (config.movementStatus.movementTypes.toggleSprintStatus && GameInformation.isPlayerSprinting()) {
            draw(context, "text.hud.simplehudenhanced.sprinting");
        }
    }

    // Draw the movement status on the screen
    private void draw(GuiGraphicsExtractor context, String textKey) {
        final String text = Utilities.translatable(textKey).getString();
        float Scale = (float) config.movementStatus.textScale / 100;

        // Screen Manager
        ScreenManager screenManager = new ScreenManager(this.client.getWindow().getGuiScaledWidth(), this.client.getWindow().getGuiScaledHeight());
        screenManager.setPadding(4);
        int xAxis = screenManager.calculateXAxis(config.movementStatus.movementStatusLocationX, Scale, this.renderer.width(text));
        int yAxis = screenManager.calculateYAxis(this.renderer.lineHeight, 1, config.movementStatus.movementStatusLocationY, Scale);
        screenManager.setScale(context, Scale);

        // Draw Info
        context.text(this.renderer, text, xAxis, yAxis, Utilities.addAlpha(config.uiConfig.textColor));

        screenManager.resetScale(context);
    }

    // Draw the Paper Doll
    public void drawPaperDoll(GuiGraphicsExtractor context) {
        if (!config.paperDoll.togglePaperDoll) {
            return;
        }

        // Guard against null player before invoking vanilla helper.
        if (this.client.player == null) {
            return;
        }

        // Config
        ScreenManager screenManager = new ScreenManager(this.client.getWindow().getGuiScaledWidth(), this.client.getWindow().getGuiScaledHeight());
        float scale = (float) config.paperDoll.textScale / 100;
        float size = 20 * scale;

        screenManager.setPadding((int) (26 * scale));
        int xAxis = screenManager.calculateXAxis(this.config.paperDoll.paperDollLocationX, 1, 0);
        screenManager.setPadding((int) (26 * scale));
        int yAxis = screenManager.calculateYAxis(0, 1, this.config.paperDoll.paperDollLocationY, 1);

        // Draw the Paper Doll
        int x1 = Math.round((xAxis - (60 * scale)));
        int y1 = Math.round((yAxis - (60 * scale)));
        int x2 = Math.round((xAxis + (60 * scale)));
        int y2 = Math.round((yAxis + (60 * scale)));
        drawEntityInternal(context, x1, y1, x2, y2, size, client.player);
    }

    // 26.1: use vanilla inventory renderer instead of removed EntityRenderState pipeline.
    private void drawEntityInternal(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, float size, Player entity) {
        context.enableScissor(x1, y1, x2, y2);

        // Reuse cached height offset for subtle vertical bobbing in the helper call.
        float verticalOffset = (1.0F - this.movementCache.getCurrentHeightOffset()) * 10.0F;
        InventoryScreen.extractEntityInInventoryFollowsMouse(context, x1, y1, x2, y2, Math.round(size), 0.0F, 0.0F, verticalOffset, entity);

        context.disableScissor();
    }
}
package com.soradgaming.simplehudenhanced.hud;

import com.soradgaming.simplehudenhanced.cache.MovementCache;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.utli.Utilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.soradgaming.simplehudenhanced.utli.Utilities.addAlpha;

public class Movement {
    private final Minecraft client;
    private final Font renderer;
    private final SimpleHudEnhancedConfig config;
    private final GuiGraphics context;
    private final MovementCache movementCache;

    public Movement(GuiGraphics context, SimpleHudEnhancedConfig config, MovementCache movementCache) {
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
    private void draw(GuiGraphics context, String textKey) {
        final String text = Utilities.translatable(textKey).getString();
        float Scale = (float) config.movementStatus.textScale / 100;

        // Screen Manager
        ScreenManager screenManager = new ScreenManager(this.client.getWindow().getGuiScaledWidth(), this.client.getWindow().getGuiScaledHeight());
        screenManager.setPadding(4);
        int xAxis = screenManager.calculateXAxis(config.movementStatus.movementStatusLocationX, Scale, this.renderer.width(text));
        int yAxis = screenManager.calculateYAxis(this.renderer.lineHeight, 1, config.movementStatus.movementStatusLocationY, Scale);
        screenManager.setScale(context, Scale);

        // Draw Info
        context.drawString(this.renderer, text, xAxis, yAxis, addAlpha(config.uiConfig.textColor));

        screenManager.resetScale(context);
    }

    // Draw the Paper Doll
    public void drawPaperDoll(GuiGraphics context) {
        if (!config.paperDoll.togglePaperDoll) {
            return;
        }

        // Get Player Entity
        Player entity = this.client.player;
        if (entity == null) {
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
        drawEntityInternal(context, x1, y1, x2, y2, size, entity);
    }

    // Custom method for rendering the paper doll. Adapted from InventoryScreen.
    private void drawEntityInternal(GuiGraphics context, int x1, int y1, int x2, int y2, float size, LivingEntity entity) {
        // --- Calculate Scissor Area ---
        context.enableScissor(x1, y1, x2, y2);
        Quaternionf quaternionZ = new Quaternionf().rotateZ(180.0F * 0.017453292F);
        Quaternionf quaternionX = new Quaternionf().rotateX(15.0F * 0.017453292F);
        quaternionZ.mul(quaternionX);

        // Build and tweak the render state (the paper doll only mutates the render state,
        // never the live entity, so no rotation save/restore is required).
        EntityRenderState entityRenderState = extractRenderState(entity);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            applyEntityRotations(livingEntityRenderState);
        }

        // --- Calculate Entity Position and Scale ---
        float yOffset = (entity.getBbHeight() + (1.0F - movementCache.getCurrentHeightOffset())) * 0.5F;
        Vector3f vector3f = new Vector3f(0.0F, yOffset, 0.0F);
        context.submitEntityRenderState(entityRenderState, size, vector3f, quaternionZ, quaternionX, x1, y1, x2, y2);

        context.disableScissor();
    }

    private static EntityRenderState extractRenderState(LivingEntity entity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(entity, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }

    private void applyEntityRotations(LivingEntityRenderState entity) {
        // TODO Config
        if (this.config.paperDoll.paperDollLocationY >= 50) {
            entity.xRot = -7.5F;
        } else {
            entity.xRot = 7.5F;
        }

        float defaultRotationYaw = 180.0F;
        if (this.config.paperDoll.paperDollLocationX >= 50) {
            defaultRotationYaw += 20.0F;
        } else {
            defaultRotationYaw -= 20.0F;
        }

        float yRotOffset = 0;

        entity.bodyRot = defaultRotationYaw;
        entity.yRot = yRotOffset;
    }
}

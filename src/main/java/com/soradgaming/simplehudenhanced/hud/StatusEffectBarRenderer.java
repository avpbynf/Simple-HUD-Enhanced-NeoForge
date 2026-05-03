package com.soradgaming.simplehudenhanced.hud;

import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.utli.StatusEffectsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import org.jspecify.annotations.NonNull;

public class StatusEffectBarRenderer {
    private static SimpleHudEnhancedConfig config;

    public static void render(GuiGraphicsExtractor graphics, MobEffectInstance effect, int x, int y, int width, int height, SimpleHudEnhancedConfig config) {
        if (!config.toggleEffectsStatus) return;

        StatusEffectBarRenderer.config = config;

        // Use a display instance — if the provided instance has duration 0 (because it was constructed from a Holder),
        // try to find the real active instance on the client player so we can get the real duration.
        MobEffectInstance displayInstance = getMobEffectInstance(effect);

        int maxDuration = StatusEffectsTracker.getInstance().getMaxDuration(displayInstance);
        float progress = maxDuration > 0 ? (float) displayInstance.getDuration() / maxDuration : 0f;
        float progress1 = calculateProgress(progress, 0.25f);
        float progress2 = calculateProgress(progress, 0.5f);
        float progress3 = calculateProgress(progress, 0.75f);
        float progress4 = calculateProgress(progress, 1f);

        drawVerticalBar(x, y, 2, 3, height - 3, progress4, graphics, displayInstance);
        drawHorizontalBar(x, y, width - 3, 3, 2, progress3, graphics, displayInstance);
        drawVerticalBar(x, y, width - 3, height - 3, 3, progress2, graphics, displayInstance);
        drawHorizontalBar(x, y, 3, width - 3, height - 3, progress1, graphics, displayInstance);
    }

    private static @NonNull MobEffectInstance getMobEffectInstance(MobEffectInstance effect) {
        MobEffectInstance displayInstance = effect;
        if (effect.getDuration() == 0) {
            try {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    for (MobEffectInstance inst : minecraft.player.getActiveEffects()) {
                        if (inst.getEffect().equals(effect.getEffect())) {
                            displayInstance = inst;
                            break;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return displayInstance;
    }

    private static float calculateProgress(float value, float threshold) {
        if (value >= threshold) {
            return 1;
        } else if (value <= threshold - 0.25f) {
            return 0;
        } else {
            return (value - threshold + 0.25f) / 0.25f;
        }
    }

    private static void drawVerticalBar(int x, int y, int startX, int startY, int endY, float progress, GuiGraphicsExtractor drawContext, MobEffectInstance effect) {
        int middleX = startX + 1;
        int middleY = Math.round(Mth.lerp(progress, (float) startY, (float) endY));
        int endX = startX;

        startX += x;
        middleX += x;
        endX += x;
        startY += y;
        middleY += y;
        endY += y;

        drawContext.fill(startX, startY, middleX, middleY, config.getColor(effect));
        drawContext.fill(middleX, middleY, endX, endY, config.effectsStatus.backgroundColor);
    }

    private static void drawHorizontalBar(int x, int y, int startX, int endX, int startY, float progress, GuiGraphicsExtractor drawContext, MobEffectInstance effect) {
        int middleY = startY + 1;
        int endY = startY;
        int middleX = Math.round(Mth.lerp(progress, (float) startX, (float) endX));

        startX += x;
        middleX += x;
        endX += x;
        startY += y;
        middleY += y;
        endY += y;

        drawContext.fill(startX, startY, middleX, middleY, config.getColor(effect));
        drawContext.fill(middleX, middleY, endX, endY, config.effectsStatus.backgroundColor);
    }
}

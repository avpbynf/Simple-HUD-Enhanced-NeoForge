package com.soradgaming.simplehudenhanced.hud;

import com.soradgaming.simplehudenhanced.cache.EquipmentCache;
import com.soradgaming.simplehudenhanced.config.EquipmentAlignment;
import com.soradgaming.simplehudenhanced.config.EquipmentOrientation;
import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Equipment {
    private final Font renderer;
    private final SimpleHudEnhancedConfig config;
    private final GuiGraphicsExtractor graphics;
    private final EquipmentCache cache;

    public Equipment(GuiGraphicsExtractor graphics, Font textRenderer, SimpleHudEnhancedConfig config, EquipmentCache equipmentCache) {
        this.renderer = textRenderer;
        this.config = config;
        this.graphics = graphics;
        this.cache = equipmentCache;
    }

    public void init() {
        // Draw Items
        draw(cache.getEquipmentInfo());
    }

    private void draw(List<EquipmentInfoStack> equipmentInfo) {
        int BoxWidth = cache.getLongestString();
        ScreenManager screenManager = cache.getScreenManager();
        int xAxis = screenManager.getXAxis();
        int yAxis = screenManager.getYAxis();
        float Scale = screenManager.getScale();
        int lineHeight = 16;
        int configX = config.equipmentStatus.equipmentStatusLocationX;


        screenManager.setScale(graphics, Scale);

        // Draw All Items on Screen
        boolean isHorizontal = config.equipmentStatus.equipmentOrientation == EquipmentOrientation.Horizontal;
        boolean isOnRight = configX >= 50;
        boolean isRightAligned = config.equipmentStatus.equipmentAlignment == EquipmentAlignment.Right || (config.equipmentStatus.equipmentAlignment == EquipmentAlignment.Auto && isOnRight);
        // Loop all items
        for (EquipmentInfoStack index : equipmentInfo) {
            ItemStack item = index.getItem();

            if (isRightAligned) {
                int lineLength = this.renderer.width(index.getText());
                int offset = (BoxWidth - lineLength);
                this.graphics.text(this.renderer, index.getText(), xAxis + offset + (isHorizontal? (-offset) : (isOnRight ? -4 : 0)), yAxis + 4, index.getColor());
                int x = xAxis + BoxWidth + 4 + (isHorizontal ? (-BoxWidth + lineLength) : (isOnRight ? -4 : 0));
                this.graphics.item(item, x, yAxis);
                drawDurabilityBar(x, yAxis, item);
            } else {
                this.graphics.text(this.renderer, index.getText(), xAxis + 16 + 4 + (isOnRight ? (isHorizontal? 0 : -4) : 0), yAxis + 4, index.getColor());
                int x = xAxis + (isOnRight ? (isHorizontal ? 0 : -4) : 0);
                this.graphics.item(item, x, yAxis);
                drawDurabilityBar(x, yAxis, item);
            }
            if (isHorizontal) {
                int lineLength = this.renderer.width(index.getText());
                xAxis += lineLength + 4 + 16 + 4;
            } else {
                yAxis += lineHeight;
            }
        }

        screenManager.resetScale(graphics);
    }

    private void drawDurabilityBar(int xAxis, int yAxis, ItemStack item) {
        if (config.equipmentStatus.Durability.showDurabilityAsBar && item.getMaxDamage() != 0) {
            // Check for 100% durability
            if (item.getDamageValue() == 0) {
                return;
            }

            int i = item.getBarWidth(); // TODO Test
            int j = item.getBarColor();
            int k = xAxis + 2;
            int l = yAxis + 13;
            this.graphics.fill(k, l, k + 13, l + 2, CommonColors.BLACK);
            this.graphics.fill(k, l, k + i, l + 1, j | CommonColors.BLACK);
        }
    }
}

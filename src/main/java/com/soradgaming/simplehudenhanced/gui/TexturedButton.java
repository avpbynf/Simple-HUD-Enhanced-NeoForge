package com.soradgaming.simplehudenhanced.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TexturedButton extends Button {
    private final Identifier texture;

    public TexturedButton(int x, int y, int width, int height, Identifier texture, 
                          OnPress onPress, Component narrationMessage) {
        super(x, y, width, height, narrationMessage, onPress, DEFAULT_NARRATION);
        this.texture = texture;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.blit(this.texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
    }
}


package com.soradgaming.simplehudenhanced.utli;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class Utilities {
    public static String getModName() {
        return "Simple HUD Enhanced";
    }

    public static String capitalise(String str) {
        // Capitalise first letter of a String
        if (str == null) return null;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static int addAlpha(int color) {
        return (color & 0x00FFFFFF) | 0xFF000000; // Add full opacity to the color
    }

    /**********************************************************
     Mod Version Management - Separate for Version Management
     **********************************************************/

    // Text Management
    public static Text translatable(String key) {
        return Text.translatable(key);
    }

    // Get Players Biome
    public static String getBiome(@Nullable ClientLevel world, Player player, boolean toggleBiomeLabel) {
        Optional<RegistryKey<net.minecraft.world.level.biome.Biome>> biome = world.getBiome(player.getBlockPos()).getKey();

        if (biome.isPresent()) {
            String biomeName = Text.translatable("biome." + biome.get().getValue().getNamespace() + "." + biome.get().getValue().getPath()).getString();
            if (toggleBiomeLabel) {
                return String.format(Text.translatable("text.hud.simplehudenhanced.biome").getString() + ": %s", Utilities.capitalise(biomeName));
            } else {
                return String.format("%s " + Text.translatable("text.hud.simplehudenhanced.biome").getString() , Utilities.capitalise(biomeName));
            }
        }

        return "";
    }

    //Get Player FPS
    public static String getFPS(Minecraft client) {
        return String.format("%d fps", client.getFps());
    }
}

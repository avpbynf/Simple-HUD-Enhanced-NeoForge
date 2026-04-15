package com.soradgaming.simplehudenhanced.utli;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static Component translatable(String key) {
        return Component.translatable(key);
    }

    // Get Players Biome
    public static String getBiome(@Nullable ClientLevel world, Player player, boolean toggleBiomeLabel) {
        if (world == null) {
            return "";
        }

        Optional<ResourceKey<Biome>> biome = world.getBiome(player.blockPosition()).unwrapKey();

        if (biome.isPresent()) {
            String biomeName = Component.translatable(getBiomeTranslationKey(biome.get())).getString();
            if (toggleBiomeLabel) {
                return String.format(Component.translatable("text.hud.simplehudenhanced.biome").getString() + ": %s", Utilities.capitalise(biomeName));
            } else {
                return String.format("%s " + Component.translatable("text.hud.simplehudenhanced.biome").getString(), Utilities.capitalise(biomeName));
            }
        }

        return "";
    }

    private static String getBiomeTranslationKey(ResourceKey<Biome> biomeKey) {
        String raw = biomeKey.toString();
        Matcher matcher = Pattern.compile("([a-z0-9_.-]+):([a-z0-9_/.-]+)").matcher(raw);

        String namespace = "minecraft";
        String path = "plains";
        while (matcher.find()) {
            namespace = matcher.group(1);
            path = matcher.group(2);
        }

        return "biome." + namespace + "." + path;
    }

    //Get Player FPS
    public static String getFPS(Minecraft client) {
        return String.format("%d fps", client.getFps());
    }
}

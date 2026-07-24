package com.soradgaming.simplehudenhanced;

import com.soradgaming.simplehudenhanced.client.SimpleHudEnhancedClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(value = "simplehudenhanced", dist = Dist.CLIENT)
public class SimpleHudEnhanced {
    public static boolean isModMenuInstalled() {
        // ModMenu is a Fabric-only mod; on NeoForge this is always false, which is what we want:
        // the pause-menu config button (ScreenEvent.Init.Post) is injected when it is absent.
        return ModList.get().isLoaded("modmenu");
    }

    public static boolean isTrinketsInstalled() {
        // Trinkets is a Fabric-only mod; on NeoForge this is always false and the vanilla
        // inventory path is used in TrinketAccessor.
        return ModList.get().isLoaded("trinkets");
    }

    public SimpleHudEnhanced(IEventBus modEventBus, ModContainer modContainer) {
        // Client-only mod (see @Mod dist): wire up all client initialisation.
        SimpleHudEnhancedClient.init(modEventBus, modContainer);
    }
}

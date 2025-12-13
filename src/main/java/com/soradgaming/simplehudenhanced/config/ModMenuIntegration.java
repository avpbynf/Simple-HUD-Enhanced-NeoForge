package com.soradgaming.simplehudenhanced.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> getConfigScreen(SimpleHudEnhancedConfig.class, parent).get();
    }
}

package com.soradgaming.simplehudenhanced.utli;

import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.hud.EquipmentInfoStack;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads the player's equipped items for the equipment HUD.
 * <p>
 * On Fabric this optionally integrated with the Trinkets accessory mod. Trinkets does not exist on
 * NeoForge (the analogue is Curios), so this always uses the vanilla inventory path. If accessory
 * support is ever wanted on NeoForge it would need a full rewrite against the Curios API.
 */
public class TrinketAccessor {
    private final Player player;
    private List<EquipmentInfoStack> equipmentInfo;
    @SuppressWarnings("unused")
    private final SimpleHudEnhancedConfig config;

    public TrinketAccessor(Player player, SimpleHudEnhancedConfig config) {
        this.player = player;
        this.config = config;
        setEquipmentInfo();
    }

    public List<EquipmentInfoStack> getEquipmentInfo() {
        return equipmentInfo;
    }

    public void setEquipmentInfo() {
        equipmentInfo = new ArrayList<>(
                Arrays.asList(
                        new EquipmentInfoStack(this.player.getInventory().getItem(39)),
                        new EquipmentInfoStack(this.player.getInventory().getItem(38)),
                        new EquipmentInfoStack(this.player.getInventory().getItem(37)),
                        new EquipmentInfoStack(this.player.getInventory().getItem(36)),
                        new EquipmentInfoStack(this.player.getOffhandItem()),
                        new EquipmentInfoStack(this.player.getMainHandItem())
                )
        );
    }
}

package com.soradgaming.simplehudenhanced.utli;

import com.google.common.collect.Maps;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Map;

public class StatusEffectsTracker {
    // Instance
    private static StatusEffectsTracker instance;

    // Map to store the active status effects
    private final Map<Holder<MobEffect>, Integer> activeStatusEffectsMax = Maps.newHashMap();

    // Constructor
    private StatusEffectsTracker() {}

    // Initialization method
    public static void initialize() {
        if (instance == null) {
            instance = new StatusEffectsTracker();
        }
    }

    // Singleton instance getter
    public static StatusEffectsTracker getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    // Method to get the max duration of a status effect
    public int getMaxDuration(MobEffectInstance effect) {
        Holder<MobEffect> effectType = effect.getEffect();

        // This function is called with active effects only.
        if (activeStatusEffectsMax.get(effectType) == null) {
            setMaxDuration(effect, effect.getDuration());
            return effect.getDuration();
        }

        if (effect.getDuration() > activeStatusEffectsMax.get(effectType)) {
            setMaxDuration(effect, effect.getDuration());
        }

        return activeStatusEffectsMax.get(effectType);
    }

    // Set the max duration of a status effect
    public void setMaxDuration(MobEffectInstance effect, int duration) {
        activeStatusEffectsMax.put(effect.getEffect(), duration);
    }

    public void removeStatusEffect(Holder<MobEffect> effect) {
        // Called on all effects that are removed, we need to filter out the ones that are not in the map
        if (activeStatusEffectsMax.get(effect) != null) {
            activeStatusEffectsMax.remove(effect);
        }
    }
}

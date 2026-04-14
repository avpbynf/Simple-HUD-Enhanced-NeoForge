package com.soradgaming.simplehudenhanced.cache;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

public class MovementCache {
    private static MovementCache instance; // Singleton instance
    private float currentHeightOffset;
    // Deadlock prevention
    private boolean currentHeightOffsetDeadlock;
    private float currentHeightOffsetOLD;

    private MovementCache() {
    }

    public static MovementCache getInstance() {
        if(instance == null) {
            instance = new MovementCache();
        }
        return instance;
    }

    public void updateCache(Player player) {
        calculateCurrentHeightOffset(player);
    }

    public synchronized float getCurrentHeightOffset() {
        if (currentHeightOffsetDeadlock) {
            return currentHeightOffsetOLD;
        } else {
            return currentHeightOffset;
        }
    }

    private void calculateCurrentHeightOffset(Player player) {
        currentHeightOffsetOLD = currentHeightOffset;
        currentHeightOffsetDeadlock = true;

        // Crouching check after Elytra since you can do both at the same time
        float height = player.getEyeHeight(Pose.STANDING);
        if (player.isFallFlying()) {
            float ticksElytraFlying = (float) (player.fallDistance + 1.0);
            float flyingAnimation = MathHelper.clamp(ticksElytraFlying * 0.09F, 0.0F, 1.0F);
            float flyingHeight = player.getEyeHeight(Pose.FALL_FLYING) / height;
            currentHeightOffset = Math.lerp(flyingAnimation, 1.0F, flyingHeight);
        } else if (player.isSwimming()) {
            float swimmingAnimation = player.isVisuallySwimming() ? 1.0F : player.swingTime;
            float swimmingHeight = player.getEyeHeight(Pose.SWIMMING) / height;
            currentHeightOffset = MathHelper.lerp(swimmingAnimation, 1.0F, swimmingHeight);
        } else if (player.isAutoSpinAttack()) {
            currentHeightOffset = player.getEyeHeight(Pose.SPIN_ATTACK) / height;
        } else if (player.isCrouching()) {
            currentHeightOffset = player.getEyeHeight(Pose.CROUCHING) / height;
        } else if (player.isSleeping()) {
            currentHeightOffset = player.getEyeHeight(Pose.SLEEPING) / height;
        } else if (player.deathTime > 0) {
            float dyingAnimation = ((float) player.deathTime + (float) 1.0 - 1.0F) / 20.0F * 1.6F;
            dyingAnimation = Math.min(1.0F, MathHelper.sqrt(dyingAnimation));
            float dyingHeight = player.getEyeHeight(Pose.DYING) / height;
            currentHeightOffset = MathHelper.lerp(dyingAnimation, 1.0F, dyingHeight);
        } else {
            currentHeightOffset = 1.0F;
        }

        currentHeightOffsetDeadlock = false;
    }
}

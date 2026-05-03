package com.soradgaming.simplehudenhanced.hud;

import com.soradgaming.simplehudenhanced.config.SimpleHudEnhancedConfig;
import com.soradgaming.simplehudenhanced.utli.Utilities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GameInfo {
    private final Minecraft client;
    private final Player player;
    private final SimpleHudEnhancedConfig config;

    public GameInfo(Minecraft client, SimpleHudEnhancedConfig config) {
        this.client = client;
        this.config = config;

        if (this.client.player != null) {
            this.player = this.client.player;
        } else {
            this.player = null;
        }
    }

    public String getCords() {
        if (!config.statusElements.coordinates.toggleCoordinates || this.player == null) {
            return "";
        }
        return String.format("%d, %d, %d", this.player.blockPosition().getX(), this.player.blockPosition().getY(), this.player.blockPosition().getZ());
    }

    public String getBiome() {
        if (!config.statusElements.Biome.toggleBiome || this.player == null) {
            return "";
        }

        if (this.client.level == null) {
            return "";
        }

        return Utilities.getBiome(this.client.level, this.player, config.statusElements.Biome.toggleBiomeLabel);
    }

    public String getDirection() {
        if (!config.statusElements.coordinates.toggleCoordinates || !config.statusElements.coordinates.toggleDirection || this.player == null) {
            return "";
        }
        String directionKey = this.player.getDirection().getName();
        if (config.statusElements.coordinates.toggleOffset) {
            return String.format(" (%s", Utilities.translatable("text.direction.simplehudenhanced." + directionKey).getString());
        } else {
            return String.format(" (%s)", Utilities.translatable("text.direction.simplehudenhanced." + directionKey).getString());
        }
    }

    public String getNether() {
        if (!config.statusElements.coordinates.toggleCoordinates || !config.statusElements.coordinates.toggleNetherCoordinateConversion || this.player == null) {
            return "";
        }
        String coordsFormat = "X: %.0f, Z: %.0f";
        if (this.player.level().dimension().equals(Level.OVERWORLD)) {
            return Utilities.translatable("text.hud.simplehudenhanced.nether").getString() + ": " + String.format(coordsFormat, this.player.getX() / 8, this.player.getZ() / 8);
        } else if (this.player.level().dimension().equals(Level.NETHER)) {
            return Utilities.translatable("text.hud.simplehudenhanced.overworld").getString() + ": " + String.format(coordsFormat, this.player.getX() * 8, this.player.getZ() * 8);
        }
        return "";
    }

    public String getChunkCords() {
        if (!config.statusElements.coordinates.toggleCoordinates || !config.statusElements.coordinates.toggleChunkCoordinates || this.player == null) {
            return "";
        }
        return Utilities.translatable("text.hud.simplehudenhanced.chunk").getString() + ": " + String.format("%d, %d, %d", this.player.blockPosition().getX() >> 4, this.player.blockPosition().getY() >> 4, this.player.blockPosition().getZ() >> 4);
    }

    public String getSubChunkCords() {
        if (!config.statusElements.coordinates.toggleCoordinates || !config.statusElements.coordinates.toggleSubChunkCoordinates || this.player == null) {
            return "";
        }
        return Utilities.translatable("text.hud.simplehudenhanced.subchunk").getString() + ": " + String.format("%d, %d, %d", this.player.blockPosition().getX() & 0xF, this.player.blockPosition().getY() & 0xF, this.player.blockPosition().getZ() & 0xF);
    }

    public String getOffset() {
        if (!config.statusElements.coordinates.toggleCoordinates || !config.statusElements.coordinates.toggleOffset || this.player == null) {
            return "";
        }
        Direction facing = this.player.getDirection();
        String offset = "";

        if (facing.getStepX() > 0) {
            offset += "+X";
        } else if (facing.getStepX() < 0) {
            offset += "-X";
        }

        if (facing.getStepZ() > 0) {
            offset += "+Z";
        } else if (facing.getStepZ() < 0) {
            offset += "-Z";
        }

        if (config.statusElements.coordinates.toggleDirection) {
            offset = " " + offset + ")";
        } else {
            offset = " (" + offset + ")";
        }

        return offset;
    }

    public String getFPS() {
        if (!config.statusElements.fps.toggleFPS) {
            return "";
        }
        return Utilities.getFPS(this.client);
    }

    // Chunk
    public String getChunkCount() {
        if (!config.statusElements.counters.chunkCount.toggleChunkCount) {
            return "";
        }

        if (this.client.level == null) {
            return "";
        }
         // Update stats before getting count
        String stats = this.player.level().getChunkSource().gatherStats(); // TODO - Test Total
        String[] parts = stats.split(",");
        int loaded = this.player.level().getChunkSource().getLoadedChunksCount();
        String total = parts.length > 1 ? parts[1].trim() : stats;

        if (config.statusElements.counters.chunkCount.toggleTotal && config.statusElements.counters.chunkCount.toggleLoaded) {
            return String.format("C: %s", stats);
        } else {
            if (config.statusElements.counters.chunkCount.toggleLoaded) {
                return String.format("C: %s", loaded);
            } else if (config.statusElements.counters.chunkCount.toggleTotal) {
                return String.format("C: %s", total);
            }
        }

        return "";
    }

    // Entity
    public String getEntityCount() {
        if (!config.statusElements.counters.toggleEntityCount) {
            return "";
        }
        if (this.client.level != null) {
            return String.format("E: %d", this.client.level.getEntityCount());
        }
        return "";
    }

    // Particles
    public String getParticleCount() {
        if (!config.statusElements.counters.toggleParticleCount) {
            return "";
        }
        return String.format("P: %s", this.client.particleEngine.countParticles());
    }

    public String getSpeed() {
        if (!config.statusElements.playerSpeed.togglePlayerSpeed || this.player == null) {
            return "";
        }

        // Use actual position deltas (displacement) between ticks instead of motion vector.
        // This reflects true displacement (accounts for collisions, friction, etc.).
        double dx = this.player.getX() - this.player.xOld;
        double dz = this.player.getZ() - this.player.zOld;
        double dy = this.player.getY() - this.player.yOld;

        // displacement per tick -> convert to meters per second by dividing by tick time (0.05s)
        double xzSpeedPerTick = Math.sqrt(dx * dx + dz * dz);
        double xzSpeedMps = xzSpeedPerTick / 0.05D;

        double currentSpeedMps = xzSpeedMps;

        if (config.statusElements.playerSpeed.togglePlayerVerticalSpeed) {
            double ySpeedPerTick = Math.abs(dy);
            double ySpeedMps = ySpeedPerTick / 0.05D;

            // deadzone to ignore tiny vertical jitter while standing
            if (ySpeedMps < 0.1D) {
                ySpeedMps = 0.0D;
            }

            currentSpeedMps = Math.sqrt(xzSpeedMps * xzSpeedMps + ySpeedMps * ySpeedMps);
        }

        return String.format("%.2f m/s", currentSpeedMps);
    }

    public String getLightLevel() {
        if (!config.statusElements.toggleLightLevel || this.player == null) {
            return "";
        }
        return String.format(Utilities.translatable("text.hud.simplehudenhanced.lightlevel").getString() + ": %d", this.player.level().getMaxLocalRawBrightness(this.player.blockPosition()));
    }

    public String getTime() {
        if (!config.statusElements.gameTime.toggleGameTime || this.player == null) {
            return "";
        }

        long time = this.player.level().getGameTime();

        if (config.statusElements.gameTime.toggleGameTime24Hour) {
            //24-hour format
            long hour = (time / 1000 + 6) % 24;
            int minute = (int) ((time % 1000) / 1000.0 * 60);
            return String.format("%d:%02d", hour, minute);
        }

        // 12-hour format
        long hour = (time / 1000 + 6) % 24;
        int minute = (int) ((time % 1000) / 1000.0 * 60);

        String ampm = "AM";
        if (hour >= 12) {
            ampm = "PM";
        }

        if (hour > 12) {
            hour -= 12;
        }
        if (hour == 0) {
            hour = 12;
        }

        return String.format("%d:%02d %s", hour, minute, ampm);
    }

    public String getSystemTime() {
        if (!config.statusElements.systemTime.toggleSystemTime) {
            return "";
        }

        java.time.LocalDateTime time = java.time.LocalDateTime.now();

        // 12-hour format
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a");

        if (config.statusElements.systemTime.toggleSystemTime24Hour) {
            // 24-hour format
            formatter = java.time.format.DateTimeFormatter.ofPattern("H:mm");
        }

        return time.format(formatter).toUpperCase();
    }

    public String getDay() {
        if (!config.statusElements.gameTime.toggleGameDayCounter || this.player == null) {
            return "";
        }
        long time = this.player.level().getLevelData().getGameTime();
        long day = (time / 24000);
        return String.format(Utilities.translatable("text.hud.simplehudenhanced.day").getString() + ": %d", day);
    }

    public String getPlayerName() {
        if (!config.statusElements.togglePlayerName || this.player == null) {
            return "";
        }
        return String.format(Utilities.translatable("text.hud.simplehudenhanced.player").getString() + ": %s", this.player.getName().getString());
    }

    public String getPing() {
        if (!config.statusElements.togglePing || this.player == null) {
            return "";
        }
        try {
            return String.format("%s " + Utilities.translatable("text.hud.simplehudenhanced.ping").getString(), this.client.getConnection().getPlayerInfo(this.player.getUUID()).getLatency());
        } catch (NullPointerException e) {
            return "";
        }
    }

    public String getTPS() {
        if (!config.statusElements.toggleTPS) {
            return "";
        }
        return String.format(Utilities.translatable("text.hud.simplehudenhanced.tps").getString() + ": %.2f", this.client.level.tickRateManager().tickrate()); // TODO test
    }

    public String getServer() {
        if (!config.statusElements.toggleServerName) {
            return "";
        }
        try {
            return String.format(Utilities.translatable("text.hud.simplehudenhanced.server").getString() + ": %s", this.client.getCurrentServer().name);
        } catch (NullPointerException e) {
            return "";
        }
    }

    public String getServerAddress() {
        if (!config.statusElements.toggleServerAddress) {
            return "";
        }
        try {
            return String.format(Utilities.translatable("text.hud.simplehudenhanced.serveraddress").getString() + ": %s", this.client.getCurrentServer().ip);
        } catch (NullPointerException e) {
            return "";
        }
    }

    public boolean isPlayerSprinting() {
        // Done this way to ensure null safety
        return this.player != null && this.player.isSprinting();
    }

    public boolean isPlayerFlying() {
        // Done this way to ensure null safety
        return this.player != null && this.player.isFallFlying();
    }

    public boolean isPlayerSwimming() {
        // Done this way to ensure null safety
        return this.player != null && this.player.isSwimming();
    }

    public boolean isPlayerSneaking() {
        // Done this way to ensure null safety
        return this.player != null && this.player.isCrouching();
    }
}

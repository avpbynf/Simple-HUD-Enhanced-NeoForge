package com.soradgaming.simplehudenhanced.mixin;

import com.soradgaming.simplehudenhanced.utli.TpsTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class TpsMixin {
    @Mixin(Connection.class)
    public static class ClientConnectionMixin {
        // Yarn ClientConnection#handlePacket -> Mojmap Connection#genericsFtw (static packet dispatch).
        @Inject(method = "genericsFtw", at = @At("HEAD"))
        private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener packetListener, CallbackInfo ci) {
            TpsTracker.INSTANCE.onPacketReceive(packet);
        }
    }
    @Mixin(ClientPacketListener.class)
    public static class ClientPlayNetworkHandlerMixin {
        // Yarn ClientPlayNetworkHandler#onGameJoin(GameJoinS2CPacket) -> Mojmap handleLogin(ClientboundLoginPacket).
        @Inject(method = "handleLogin", at = @At("TAIL"))
        private void triggerJoinEvent(ClientboundLoginPacket packet, CallbackInfo info) {
            TpsTracker.INSTANCE.onGameJoined();
        }
    }
}

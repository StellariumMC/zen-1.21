package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.EntityEvent;

import java.util.Optional;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {
    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
    private void zen$onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity entity) {
        if (entity != null) {
            String name = packet.trackedValues() != null ? packet.trackedValues().stream()
                    .filter(entry -> entry.id() == 2)
                    .map(entry -> entry.value() instanceof Optional<?> ? ((Optional<?>) entry.value()).orElse(null) : null)
                    .filter(value -> value instanceof Text)
                    .map(text -> ((Text) text).getString())
                    .findFirst().orElse("") : "";

            if (EventBus.INSTANCE.post(new EntityEvent.Packet.Metadata(packet, entity, name))) {
                if (client != null && client.world != null) {
                    client.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }
}
package xyz.meowing.zen.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import xyz.meowing.zen.events.EventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.EntityEvent;

import java.util.Optional;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener extends ClientCommonPacketListenerImpl {
    protected MixinClientPacketListener(
            Minecraft client,
            Connection connection,
            CommonListenerCookie connectionState
    ) {
        super(client, connection, connectionState);
    }

    @Inject(
            method = "handleSetEntityData",
            at = @At("TAIL")
    )
    private void zen$onEntityTrackerUpdate(
            ClientboundSetEntityDataPacket packet,
            CallbackInfo ci,
            @Local Entity entity
    ) {
        if (entity != null) {
            String name = packet.packedItems().stream()
                    .filter(entry -> entry.id() == 2)
                    .map(entry -> entry.value() instanceof Optional<?> ? ((Optional<?>) entry.value()).orElse(null) : null)
                    .filter(value -> value instanceof Component)
                    .map(text -> ((Component) text).getString())
                    .findFirst().orElse("");

            if (EventBus.INSTANCE.post(new EntityEvent.Packet.Metadata(packet, entity, name))) {
                if (this.minecraft.level != null) {
                    this.minecraft.level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }
}
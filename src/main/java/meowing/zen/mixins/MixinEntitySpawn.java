package meowing.zen.mixins;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinEntitySpawn {
    @Inject(method = "channelRead0*", at = @At("HEAD"))
    private void onPacketReceive(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof EntityTrackerUpdateS2CPacket trackerPacket) {
            EventTypes.EntityTrackerUpdateEvent event = new EventTypes.EntityTrackerUpdateEvent(trackerPacket);
            EventBus.fire(event);
        }
    }
}
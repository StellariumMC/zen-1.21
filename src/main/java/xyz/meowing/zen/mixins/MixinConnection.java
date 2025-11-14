package xyz.meowing.zen.mixins;

import net.minecraft.network.PacketSendListener;
import xyz.meowing.zen.events.EventBus;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.zen.events.core.PacketEvent;

//#if MC >= 1.21.8
//$$ import io.netty.channel.ChannelFutureListener;
//#endif

@Mixin(Connection.class)
public class MixinConnection {
    @Inject(
            method = "channelRead0*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zen$onReceivePacket(
            ChannelHandlerContext context,
            Packet<?> packet,
            CallbackInfo ci
    ) {
        if (EventBus.INSTANCE.onPacketReceived(packet)) ci.cancel();
    }

    @Inject(
            method = "channelRead0*",
            at = @At("TAIL")
    )
    private void zen$onReceivePacketPost(
            ChannelHandlerContext context,
            Packet<?> packet,
            CallbackInfo ci
    ) {
        EventBus.INSTANCE.post(new PacketEvent.ReceivedPost(packet));
    }

    @Inject(
            method = "sendPacket",
            at = @At("HEAD"),
            cancellable = true
    )
    //#if MC >= 1.21.8
    //$$ private void zen$onPacketSend(Packet<?> packet, ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {
    //#else
    private void zen$onPacketSend(
            Packet<?> packet,
            PacketSendListener callbacks,
            boolean flush,
            CallbackInfo ci
    ) {
    //#endif
        if (EventBus.INSTANCE.post(new PacketEvent.Sent(packet))) ci.cancel();
    }
}
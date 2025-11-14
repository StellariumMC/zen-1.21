@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.network.protocol.Packet
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event

abstract class PacketEvent {
    /**
     * Posted when the game has received a packet from the server.
     *
     * @see xyz.meowing.zen.mixins.MixinConnection
     * @since 1.2.0
     */
    class Received(
        val packet: Packet<*>
    ) : CancellableEvent()

    /**
     * Posted after the game has finished handling the received packet.
     *
     * @see xyz.meowing.zen.mixins.MixinConnection
     * @since 1.2.0
     */
    class ReceivedPost(
        val packet: Packet<*>
    ) : Event()

    /**
     * Posted when the game has sent a packet to the server.
     *
     * @see xyz.meowing.zen.mixins.MixinConnection
     * @since 1.2.0
     */
    class Sent(
        val packet: Packet<*>
    ) : Event()

    /**
     * Posted after the game has finished sending the packet to the server.
     *
     * @see xyz.meowing.zen.mixins.MixinConnection
     * @since 1.2.0
     */
    class SentPost(
        val packet: Packet<*>
    ) : Event()
}
package xyz.meowing.zen.api.hypixel

import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.fabric.event.HypixelModAPICallback
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.LocationEvent
import kotlin.jvm.optionals.getOrNull

@Module
object HypixelAPI {
    init {
        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket::class.java)
        HypixelModAPICallback.EVENT.register { event ->
            when (event) {
                is ClientboundLocationPacket -> {
                    EventBus.post(LocationEvent.ServerChange(
                        event.serverName,
                        event.serverType.getOrNull(),
                        event.lobbyName.getOrNull(),
                        event.mode.getOrNull(),
                        event.map.getOrNull(),
                    ))
                }

                is ClientboundHelloPacket -> {
                    EventBus.post(LocationEvent.HypixelJoin(event.environment))
                }
            }
        }
    }
}
package xyz.meowing.zen.features.visuals

import xyz.meowing.knit.api.KnitClient
import xyz.meowing.zen.features.Feature
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.PacketEvent

@Module
object TimeChanger : Feature(
    "timeChanger",
    "Time changer",
    "Changes the time of day",
    "Visuals"
) {
    private val timeOfDay by config.slider("Time of day", 1.0, 0.1, 24.0, true)

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is ClientboundSetTimePacket) {
                event.cancel()
                KnitClient.client.level?.setTimeFromServer((1000L * timeOfDay).toLong(), (1000L * timeOfDay).toLong(), false)
            }
        }
    }
}
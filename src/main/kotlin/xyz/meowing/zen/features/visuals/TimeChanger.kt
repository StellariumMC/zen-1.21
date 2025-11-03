package xyz.meowing.zen.features.visuals

import xyz.meowing.knit.api.KnitClient
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.PacketEvent

@Module
object TimeChanger : Feature(
    "timeChanger"
) {
    private val timeOfDay by ConfigDelegate<Double>("timeChanger.timeOfDay")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Time changer",
                "Changes the time of day",
                "Visuals",
                ConfigElement(
                    "timeChanger",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Time of day",
                ConfigElement(
                    "timeChanger.timeOfDay",
                    ElementType.Slider(0.1, 24.0, 1.0, true)
                )
            )
    }

    override fun initialize() {
        register<PacketEvent.Received> {
            if (it.packet is WorldTimeUpdateS2CPacket) {
                it.cancel()
                KnitClient.client.world?.setTime((1000L * timeOfDay).toLong(), (1000L * timeOfDay).toLong(), false)
            }
        }
    }
}
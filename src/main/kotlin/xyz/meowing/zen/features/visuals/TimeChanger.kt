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
object TimeChanger : Feature("timechanger") {
    val timeSlider by ConfigDelegate<Double>("timeslider")

    override fun addConfig() {
        ConfigManager
            .addFeature("Time Changer", "", "Visuals", ConfigElement(
                "timechanger",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Time of day", "", "Size", ConfigElement(
                "timeslider",
                ElementType.Slider(0.1, 24.0, 1.0, true)
            ))
    }

    override fun initialize() {
        register<PacketEvent.Received> {
            if (it.packet is WorldTimeUpdateS2CPacket) {
                it.cancel()
                KnitClient.client.world?.setTime((1000L * timeSlider).toLong(), (1000L * timeSlider).toLong() , false)
            }
        }
    }
}
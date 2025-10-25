package xyz.meowing.zen.features.visuals

import xyz.meowing.knit.api.KnitClient
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.events.PacketEvent
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@Zen.Module
object TimeChanger : Feature("timechanger") {
    val timeslider by ConfigDelegate<Double>("timeslider")

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
            if(it.packet is WorldTimeUpdateS2CPacket) {
                it.cancel()
                KnitClient.client.world?.setTime((1000L * timeslider).toLong(), (1000L * timeslider).toLong() , false)
            }
        }
    }
}
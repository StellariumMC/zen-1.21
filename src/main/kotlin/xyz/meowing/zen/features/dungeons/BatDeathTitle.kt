package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.PacketEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TitleUtils.showTitle
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.utils.LocationUtils.subarea

@Zen.Module
object BatDeathTitle : Feature("batdeadtitle", true, "catacombs") {

    override fun addConfig() {
        ConfigManager
            .addFeature("Bat Death Title", "Shows a title when bats die in dungeons", "Dungeons", ConfigElement(
                "batdeadtitle",
                ElementType.Switch(false))
            )
    }

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is PlaySoundS2CPacket) {
                val packet = event.packet
                if (!packet.sound.toString().contains("minecraft:entity.bat.death") && !packet.sound.toString().contains("minecraft:entity.bat.hurt")) return@register
                if (!isEnabled()) return@register
                if (subarea?.lowercase()?.contains("boss") == true) return@register
                showTitle("§cBat Dead!", null, 1000)
            }
        }
    }
}
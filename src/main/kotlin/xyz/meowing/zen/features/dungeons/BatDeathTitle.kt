package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.utils.LocationUtils
import xyz.meowing.zen.utils.TitleUtils
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.passive.BatEntity

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
        register<EntityEvent.Death> {
            if (it.entity is BatEntity && it.entity.vehicle !is ArmorStandEntity &&
                LocationUtils.subarea?.lowercase()?.contains("boss") != true) {
                TitleUtils.showTitle("§cBat Dead!", null, 1000)
            }
        }
    }
}
package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TitleUtils
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.ambient.Bat
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonAPI
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object BatDeathTitle : Feature(
    "batDeadTitle",
    island = SkyBlockIsland.THE_CATACOMBS
) {

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Bat death title",
                "Shows a title when bats die in dungeons",
                "Dungeons",
                ConfigElement(
                    "batDeadTitle",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<EntityEvent.Death> {
            if (it.entity is Bat && it.entity.vehicle !is ArmorStand && !DungeonAPI.inBoss) {
                TitleUtils.showTitle("§cBat Dead!", null, 1000)
            }
        }
    }
}
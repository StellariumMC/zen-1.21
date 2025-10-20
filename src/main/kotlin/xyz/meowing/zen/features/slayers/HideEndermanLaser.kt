package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.slayers.carrying.CarryCounter
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.mob.EndermanEntity
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import java.util.concurrent.ConcurrentHashMap

@Zen.Module
object HideEndermanLaser : Feature("hideendermanlaser", true) {
    private val hideForOption by ConfigDelegate<Int>("hideendermanlaserboss")
    private val endermanCache = ConcurrentHashMap<Int, EndermanEntity>()
    private val spawnerCache = ConcurrentHashMap<Int, String>()
    private var lastCacheUpdate = 0L

    override fun addConfig() {
        ConfigManager
            .addFeature("Hide Enderman Laser", "", "Slayers", ConfigElement(
                "hideendermanlaser",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Hide For", "", "Options", ConfigElement(
                "hideendermanlaserboss",
                ElementType.Dropdown(
                    listOf("All bosses", "Carries", "Mine", "Mine and carries", "Not mine/carries"),
                    0
                )
            ))
    }


    override fun initialize() {
        register<RenderEvent.GuardianLaser> { event ->
            val guardianEntity = event.entity
            val closestSlayer = getCachedClosestEnderman(guardianEntity)

            if (closestSlayer != null && shouldHideLaser(closestSlayer.id)) event.cancel()
        }

        register<WorldEvent.Change> {
            clearCache()
        }
    }

    private fun getCachedClosestEnderman(guardianEntity: net.minecraft.entity.Entity): EndermanEntity? {
        val currentTick = TickUtils.getCurrentServerTick()
        if (currentTick - lastCacheUpdate >= 10) {
            lastCacheUpdate = currentTick
            val slayerEntities = EntityDetection.getSlayerEntities()
            endermanCache.keys.retainAll(slayerEntities.keys.map { it.id }.toSet())
            slayerEntities.keys.filterIsInstance<EndermanEntity>().forEach { enderman ->
                endermanCache[enderman.id] = enderman
            }
        }
        return endermanCache.values.minByOrNull { guardianEntity.squaredDistanceTo(it) }
    }

    private fun shouldHideLaser(slayerEntityId: Int): Boolean {
        if (hideForOption == 0) return true

        val spawnerNametag = getCachedSpawnerNametag(slayerEntityId)
        if (!spawnerNametag.contains("Spawned by")) return false

        val playerName = player?.name?.string ?: return true

        val cleanSpawnerName = spawnerNametag.removeFormatting()
        val cleanPlayerName = playerName.removeFormatting()

        return when (hideForOption) {
            1 -> CarryCounter.carryees.any {
                val carryeeName = it.name.removeFormatting()
                cleanSpawnerName.endsWith("by: $carryeeName")
            }
            2 -> cleanSpawnerName.endsWith("by: $cleanPlayerName")
            3 -> cleanSpawnerName.endsWith("by: $cleanPlayerName") || CarryCounter.carryees.any {
                val carryeeName = it.name.removeFormatting()
                cleanSpawnerName.endsWith("by: $carryeeName")
            }
            4 -> !cleanSpawnerName.endsWith("by: $cleanPlayerName") && !CarryCounter.carryees.any {
                val carryeeName = it.name.removeFormatting()
                cleanSpawnerName.endsWith("by: $carryeeName")
            }
            else -> false
        }
    }

    private fun getCachedSpawnerNametag(slayerEntityId: Int): String {
        return spawnerCache.getOrPut(slayerEntityId) {
            val entity = world?.getEntityById(slayerEntityId + 3)
            val nameTag = entity?.customName?.string ?: ""

            if (nameTag.contains("Spawned by") &&
                !nameTag.removeFormatting().contains("Armorstand")) {
                nameTag
            } else {
                ""
            }
        }
    }

    fun clearCache() {
        endermanCache.clear()
        spawnerCache.clear()
        lastCacheUpdate = 0
    }

    override fun onUnregister() {
        super.onUnregister()
        clearCache()
    }
}

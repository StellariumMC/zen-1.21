package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.slayers.carrying.CarryCounter
import net.minecraft.entity.mob.EndermanEntity
import java.util.concurrent.ConcurrentHashMap

@Zen.Module
object HideEndermanLaser : Feature("hideendermanlaser", true) {
    private val hideForOption by ConfigDelegate<Int>("hideendermanlaserboss")
    private val endermanCache = ConcurrentHashMap<Int, EndermanEntity>()
    private val spawnerCache = ConcurrentHashMap<Int, String>()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Hide Enderman Laser", "", "Slayers", xyz.meowing.zen.ui.ConfigElement(
                "hideendermanlaser",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Hide For", "Options", xyz.meowing.zen.ui.ConfigElement(
                "hideendermanlaserboss",
                ElementType.Dropdown(
                    listOf("All bosses", "Carries", "Mine", "Mine and carries", "Not mine/carries"),
                    0
                )
            ))
        return configUI
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
        val slayerEntities = EntityDetection.getSlayerEntities()

        endermanCache.keys.retainAll(slayerEntities.keys.map { it.id }.toSet())

        slayerEntities.keys.filterIsInstance<EndermanEntity>().forEach { enderman ->
            endermanCache[enderman.id] = enderman
        }

        return endermanCache.values.minByOrNull { guardianEntity.distanceTo(it) }
    }

    private fun shouldHideLaser(slayerEntityId: Int): Boolean {
        if (hideForOption == 0) return true

        val spawnerNametag = getCachedSpawnerNametag(slayerEntityId)
        if (!spawnerNametag.contains("Spawned by")) return false

        val playerName = player?.name ?: return true

        return when (hideForOption) {
            1 -> CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") }
            2 -> spawnerNametag.endsWith("by: $playerName")
            3 -> spawnerNametag.endsWith("by: $playerName") || CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") }
            4 -> !spawnerNametag.endsWith("by: $playerName") && !CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") }
            else -> false
        }
    }

    private fun getCachedSpawnerNametag(slayerEntityId: Int): String {
        return spawnerCache.getOrPut(slayerEntityId) {
            world?.getEntityById(slayerEntityId + 3)?.customName?.string ?: ""
        }
    }

    fun clearCache() {
        endermanCache.clear()
        spawnerCache.clear()
    }

    override fun onUnregister() {
        super.onUnregister()
        clearCache()
    }
}
package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.*
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.slayers.carrying.CarryCounter
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.mob.EndermanEntity
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import java.util.concurrent.ConcurrentHashMap

@Zen.Module
object HideEndermanLaser : Feature("hideendermanlaser", true) {
    private val hideForOption by ConfigDelegate<Int>("hideendermanlaserboss")
    private val endermanCache = ConcurrentHashMap<Int, EndermanEntity>()
    private val nametagData = ConcurrentHashMap<Int, String>()
    private var lastCacheUpdate = 0L
    private var cacheInitialized = false

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

            if (closestSlayer != null && shouldHideLaser(closestSlayer.id)) {
                event.cancel()
            }
        }

        register<WorldEvent.Change> {
            clearCache()
            cacheInitialized = false
        }

        register<EntityEvent.Metadata> { event ->
            nametagData[event.entity.id] = event.name.removeFormatting()
        }

        register<SkyblockEvent.Slayer.Spawn> { event ->
            if (event.entity is EndermanEntity) {
                updateCache()
                cacheInitialized = true
            }
        }
    }

    private fun getCachedClosestEnderman(guardianEntity: net.minecraft.entity.Entity): EndermanEntity? {
        val currentTick = TickUtils.getCurrentServerTick()
        if (!cacheInitialized || currentTick - lastCacheUpdate >= 5) {
            updateCache()
            lastCacheUpdate = currentTick
            cacheInitialized = true
        }

        return endermanCache.values.minByOrNull { guardianEntity.squaredDistanceTo(it) }
    }

    private fun updateCache() {
        val slayerEntities = EntityDetection.getSlayerEntities()

        endermanCache.clear()
        slayerEntities.keys.filterIsInstance<EndermanEntity>().forEach { enderman ->
            endermanCache[enderman.id] = enderman
        }
    }

    private fun shouldHideLaser(slayerEntityId: Int): Boolean {
        if (hideForOption == 0) return true

        val spawnerNametag = nametagData[slayerEntityId + 3] ?: ""
        if (!spawnerNametag.contains("Spawned by")) return false

        val playerName = player?.name?.string ?: return true
        val cleanSpawnerName = spawnerNametag.removeFormatting()
        val cleanPlayerName = playerName.removeFormatting()

        val isMyBoss = cleanSpawnerName.endsWith("by: $cleanPlayerName")
        val isCarryBoss = CarryCounter.carryees.any {
            cleanSpawnerName.endsWith("by: ${it.name.removeFormatting()}")
        }

        return when (hideForOption) {
            1 -> isCarryBoss
            2 -> isMyBoss
            3 -> isMyBoss || isCarryBoss
            4 -> !isMyBoss && !isCarryBoss
            else -> false
        }
    }

    fun clearCache() {
        endermanCache.clear()
        nametagData.clear()
        lastCacheUpdate = 0
    }

    override fun onUnregister() {
        super.onUnregister()
        clearCache()
    }
}

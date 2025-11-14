package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.api.skyblock.EntityDetection
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.slayers.carrying.CarryCounter
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.world.entity.monster.EnderMan
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.util.concurrent.ConcurrentHashMap

@Module
object HideEndermanLaser : Feature(
    "hideEndermanLaser",
    true
) {
    private val hideForOption by ConfigDelegate<Int>("hideEndermanLaser.forBossType")
    private val endermanCache = ConcurrentHashMap<Int, EnderMan>()
    private val nametagData = ConcurrentHashMap<Int, String>()
    private var lastCacheUpdate = 0L
    private var cacheInitialized = false

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Hide enderman laser",
                "",
                "Slayers",
                ConfigElement(
                    "hideEndermanLaser",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Hide for",
                ConfigElement(
                    "hideEndermanLaser.forBossType",
                    ElementType.Dropdown(
                        listOf("All bosses", "Carries", "Mine", "Mine and carries", "Not mine/carries"),
                        0
                    )
                )
            )
    }


    override fun initialize() {
        register<RenderEvent.GuardianLaser> { event ->
            val guardianEntity = event.entity
            val closestSlayer = getCachedClosestEnderman(guardianEntity)

            if (closestSlayer != null && shouldHideLaser(closestSlayer.id)) {
                event.cancel()
            }
        }

        register<LocationEvent.WorldChange> {
            clearCache()
            cacheInitialized = false
        }

        register<EntityEvent.Packet.Metadata> { event ->
            nametagData[event.entity.id] = event.name.removeFormatting()
        }

        register<SkyblockEvent.Slayer.Spawn> { event ->
            if (event.entity is EnderMan) {
                updateCache()
                cacheInitialized = true
            }
        }
    }

    private fun getCachedClosestEnderman(guardianEntity: net.minecraft.world.entity.Entity): EnderMan? {
        val currentTick = TickUtils.getCurrentServerTick()
        if (!cacheInitialized || currentTick - lastCacheUpdate >= 5) {
            updateCache()
            lastCacheUpdate = currentTick
            cacheInitialized = true
        }

        return endermanCache.values.minByOrNull { guardianEntity.distanceToSqr(it) }
    }

    private fun updateCache() {
        val slayerEntities = EntityDetection.getSlayerEntities()

        endermanCache.clear()
        slayerEntities.keys.filterIsInstance<EnderMan>().forEach { enderman ->
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
        val isCarryBoss = CarryCounter.carries.any {
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

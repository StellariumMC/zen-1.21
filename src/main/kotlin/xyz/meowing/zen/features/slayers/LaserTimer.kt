package xyz.meowing.zen.features.slayers

import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Render3D
import java.awt.Color

@Module
object LaserTimer : Feature(
    "laserTimer",
    "Laser phase timer",
    "Laser phase timer for voidgloom slayer",
    "Slayers",
    skyblockOnly = true
) {
    private const val TOTAL_TIME = 8.2
    private var bossID = 0

    override fun initialize() {
        createCustomEvent<RenderEvent.Entity.Post>("render") {
            drawString()
        }

        register<SkyblockEvent.Slayer.Spawn> { event ->
            bossID = event.entityID - 3
            registerEvent("render")
        }

        register<SkyblockEvent.Slayer.Death> {
            bossID = 0
            unregisterEvent("render")
        }

        register<SkyblockEvent.Slayer.Fail> {
            bossID = 0
            unregisterEvent("render")
        }

        register<SkyblockEvent.Slayer.Cleanup> {
            bossID = 0
            unregisterEvent("render")
        }
    }

    private fun drawString() {
        val entity = world?.getEntity(bossID) ?: return
        if (player?.hasLineOfSight(entity) != true) return
        val ridingEntity = entity.vehicle ?: return
        val time = maxOf(0.0, TOTAL_TIME - (ridingEntity.tickCount / 20.0))
        val text = "§bLaser: §c${"%.1f".format(time)}"

        Render3D.drawString(
            text,
            entity.position(),
            Color.WHITE.rgb,
            2.0f,
            1.0f
        )
    }
}
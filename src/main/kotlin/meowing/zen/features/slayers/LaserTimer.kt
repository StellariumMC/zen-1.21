package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.events.SkyblockEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Render3D
import java.awt.Color

@Zen.Module
object LaserTimer : Feature("lasertimer", true) {
    private var bossID = 0
    private val totaltime = 8.2

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Laser phase timer", ConfigElement(
                "lasertimer",
                "Laser phase timer",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

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
        val ent = world?.getEntityById(bossID) ?: return
        if (player?.canSee(ent) != true) return
        val ridingentity = ent.vehicle ?: return
        val time = maxOf(0.0, totaltime - (ridingentity.age / 20.0))
        val text = "§bLaser: §c${"%.1f".format(time)}"

        Render3D.drawString(text, ent.pos, Color.WHITE.rgb, 2.0f, 1.0f)
    }
}
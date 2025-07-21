package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Render3D
import java.awt.Color

@Zen.Module
object LaserTimer : Feature("lasertimer") {
    private var bossID = 0
    private val totaltime = 8.2
    private val renderCall: EventBus.EventCall = EventBus.register<RenderEvent.WorldPostEntities> ({ drawString() }, false)

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Enderman", ConfigElement(
                "lasertimer",
                "Laser phase timer",
                "Time until laser phase ends",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Leave> { event ->
            if (event.entity.id == bossID) {
                bossID = 0
                renderCall.unregister()
            }
        }
    }

    fun handleSpawn(entityID: Int) {
        bossID = entityID - 3
        renderCall.register()
    }

    private fun drawString() {
        val ent = mc.world?.getEntityById(bossID) ?: return
        if (player?.canSee(ent) != true) return
        val ridingentity = ent.vehicle ?: return
        val time = maxOf(0.0, totaltime - (ridingentity.age / 20.0))
        val text = "§bLaser: §c${"%.1f".format(time)}"

        Render3D.drawString(text, ent.pos, Color.WHITE.rgb, 2.0f, 1.0f)
    }
}
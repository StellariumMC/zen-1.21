package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.events.SkyblockEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Render3D
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.util.math.Vec3d
import java.awt.Color

@Zen.Module
object FireFreezeOverlay : Feature("firefreezeoverlay") {
    private var activatedPos: Vec3d? = null
    private var overlayTimerId: Long? = null
    private var freezeTimerId: Long? = null
    private var frozenEntities = mutableSetOf<Entity>()
    private val firefreezeoverlaycolor by ConfigDelegate<Color>("firefreezeoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Fire freeze overlay", ConfigElement(
                "firefreezeoverlay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Fire freeze overlay", "Color", ConfigElement(
                "firefreezeoverlaycolor",
                "Fire Freeze Overlay color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<SkyblockEvent.ItemAbilityUsed> { event ->
            if (event.ability.itemId == "FIRE_FREEZE_STAFF") {
                activatedPos = player?.pos
                frozenEntities.clear()

                overlayTimerId = createTimer(100) {
                    overlayTimerId = null
                }

                TickUtils.scheduleServer(100) {
                    freezeTimerId = createTimer(200,
                        onComplete = {
                            frozenEntities.clear()
                            freezeTimerId = null
                        }
                    )

                    world?.entities?.forEach { ent ->
                        if (ent is LivingEntity && ent !is ArmorStandEntity && !ent.isInvisible && ent != player && ent.squaredDistanceTo(activatedPos) <= 25) {
                            frozenEntities.add(ent)
                        }
                    }
                }
            }
        }

        register<RenderEvent.World> { event ->
            val context = event.context ?: return@register
            val timer = overlayTimerId?.let { getTimer(it) } ?: return@register
            val pos = activatedPos ?: return@register
            val text = "§b${"%.1f".format(timer.ticks / 20.0)}s"

            Render3D.drawFilledCircle(
                context,
                pos,
                5f,
                72,
                firefreezeoverlaycolor.darker().toColorInt(),
                firefreezeoverlaycolor.toColorInt()
            )

            Render3D.drawString(
                text,
                pos.add(0.0, 1.0, 0.0),
                0x000000
            )
        }

        register<RenderEvent.World> { event ->
            val timer = freezeTimerId?.let { getTimer(it) } ?: return@register
            frozenEntities.removeAll { !it.isAlive }
            frozenEntities.forEach { ent ->
                val freezeText = "§b${"%.1f".format(timer.ticks / 20.0)}s"
                Render3D.drawString(freezeText, ent.pos, 0x000000)
            }
        }

        register<RenderEvent.EntityGlow> { event ->
            if (event.entity in frozenEntities) {
                event.shouldGlow = true
                event.glowColor = firefreezeoverlaycolor.toColorInt()
            }
        }
    }

    override fun onUnregister() {
        overlayTimerId = null
        freezeTimerId = null
        frozenEntities.clear()
        super.onUnregister()
    }
}
package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.ui.ConfigMenuManager
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.util.math.Vec3d
import java.awt.Color

@Zen.Module
object FireFreezeOverlay : Feature("firefreezeoverlay", true) {
    private var activatedPos: Vec3d? = null
    private var overlayTimerId: Long? = null
    private var freezeTimerId: Long? = null
    private var frozenEntities = mutableSetOf<Entity>()
    private val firefreezeoverlaycolor by ConfigDelegate<Color>("firefreezeoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Fire freeze overlay", "", "Visuals", xyz.meowing.zen.ui.ConfigElement(
                "firefreezeoverlay",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "firefreezeoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))

        return configUI
    }

    override fun initialize() {
        register<SkyblockEvent.ItemAbilityUsed> { event ->
            if (event.ability.itemId == "FIRE_FREEZE_STAFF") {
                //#if MC >= 1.21.9
                //$$ activatedPos = player?.entityPos
                //#else
                activatedPos = player?.pos
                //#endif
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
            //#if MC < 1.21.9
            val context = event.context ?: return@register
            val timer = overlayTimerId?.let { getTimer(it) } ?: return@register
            val pos = activatedPos ?: return@register

            Render3D.drawFilledCircle(
                context,
                pos,
                5f,
                72,
                firefreezeoverlaycolor.darker().toColorInt(),
                firefreezeoverlaycolor.toColorInt()
            )

            val text = "§b${"%.1f".format(timer.ticks / 20.0)}s"
            Render3D.drawString(
                text,
                activatedPos?.add(0.0, 1.0, 0.0) ?: return@register,
                0x000000
            )
            //#endif
        }

        register<RenderEvent.World> { event ->
            val timer = freezeTimerId?.let { getTimer(it) } ?: return@register
            frozenEntities.removeAll { !it.isAlive }
            frozenEntities.forEach { ent ->
                val freezeText = "§b${"%.1f".format(timer.ticks / 20.0)}s"
                Render3D.drawString(
                    freezeText,
                    //#if MC >= 1.21.9
                    //$$ ent.entityPos,
                    //#else
                    ent.pos,
                    //#endif
                    0x000000
                )
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
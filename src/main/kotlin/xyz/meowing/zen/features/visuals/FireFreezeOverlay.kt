package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.util.math.Vec3d
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.config.ConfigElement
import java.awt.Color

@Zen.Module
object FireFreezeOverlay : Feature("firefreezeoverlay", true) {
    private var activatedPos: Vec3d? = null
    private var overlayTimerId: Long? = null
    private var freezeTimerId: Long? = null
    private var frozenEntities = mutableSetOf<Entity>()
    private val firefreezeoverlaycolor by ConfigDelegate<Color>("firefreezeoverlaycolor")

    override fun addConfig() {
        ConfigManager
            .addFeature("Fire freeze overlay", "", "Visuals", ConfigElement(
                "firefreezeoverlay",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", ConfigElement(
                "firefreezeoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
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
            val timer = overlayTimerId?.let { getTimer(it) } ?: return@register
            val pos = activatedPos ?: return@register

            Render3D.drawFilledCircle(
                event.context.consumers(),
                event.context.matrixStack(),
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
package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.TickUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color

@Module
object FireFreezeOverlay : Feature(
    "fireFreezeOverlay",
    true
) {
    private var activatedPos: Vec3? = null
    private var overlayTimerId: Long? = null
    private var freezeTimerId: Long? = null
    private var frozenEntities = mutableSetOf<Entity>()
    private val color by ConfigDelegate<Color>("firefreezeoverlaycolor")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Fire freeze overlay",
                "",
                "Visuals",
                ConfigElement(
                    "fireFreezeOverlay",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Color",
                ConfigElement(
                    "fireFreezeOverlay.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }

    override fun initialize() {
        register<SkyblockEvent.ItemAbilityUsed> { event ->
            if (event.ability.itemId == "FIRE_FREEZE_STAFF") {
                activatedPos = player?.position()
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

                    world?.entitiesForRendering()?.forEach { ent ->
                        if (ent is LivingEntity && ent !is ArmorStand && !ent.isInvisible && ent != player && ent.distanceToSqr(activatedPos) <= 25) {
                            frozenEntities.add(ent)
                        }
                    }
                }
            }
        }

        register<RenderEvent.World.Last> { event ->
            val timer = overlayTimerId?.let { getTimer(it) } ?: return@register
            val pos = activatedPos ?: return@register

            Render3D.drawFilledCircle(
                event.context.consumers(),
                event.context.matrixStack(),
                pos,
                5f,
                72,
                color.darker().rgb,
                color.rgb
            )

            val text = "§b${"%.1f".format(timer.ticks / 20.0)}s"
            Render3D.drawString(
                text,
                activatedPos?.add(0.0, 1.0, 0.0) ?: return@register,
                0x000000
            )
        }

        register<RenderEvent.World.Last> {
            val timer = freezeTimerId?.let { getTimer(it) } ?: return@register
            frozenEntities.removeAll { !it.isAlive }
            frozenEntities.forEach { ent ->
                val freezeText = "§b${"%.1f".format(timer.ticks / 20.0)}s"
                Render3D.drawString(
                    freezeText,
                    ent.position(),
                    0x000000
                )
            }
        }

        register<RenderEvent.Entity.Pre> { event ->
            val entity = event.entity

            if (entity in frozenEntities) {
                entity.glowThisFrame = true
                entity.glowingColor = color.rgb
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
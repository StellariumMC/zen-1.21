package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.api.skyblock.EntityDetection.sbMobID
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.EntityHitResult
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.MouseEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color

@Module
object BestiaryMobHighlight : Feature(
    "bestiaryMobHighlighter",
    true
) {
    private val trackedMobs = mutableListOf<String>()
    private val highlightColor by ConfigDelegate<Color>("bestiaryMobHighlighter.color")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Bestiary Mob Highlight",
                "Middle click on a mob in the world to toggle highlighting for it",
                "Visuals",
                ConfigElement(
                    "bestiaryMobHighlighter",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Highlight Color",
                ConfigElement(
                    "bestiaryMobHighlighter.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }

    override fun initialize() {
        register<RenderEvent.Entity.Pre> { event ->
            val entity = event.entity
            val mob = entity.sbMobID ?: return@register
            if (trackedMobs.contains(mob)) {
                val visible = KnitClient.player?.hasLineOfSight(entity)?: false

                if (!visible) return@register
                entity.glowThisFrame = true
                entity.glowingColor = highlightColor.rgb
            }
        }

        register<MouseEvent.Click> { event ->
            if (event.button == 2) {
                val mob = getTargetEntity() ?: return@register
                val id = mob.sbMobID ?: return@register KnitChat.fakeMessage("$prefix §cThis mob could not be identified for the bestiary tracker!")
                if (trackedMobs.contains(id)) {
                    trackedMobs.remove(id)
                    KnitChat.fakeMessage("$prefix §cStopped highlighting ${id}!")
                } else {
                    trackedMobs.add(id)
                    KnitChat.fakeMessage("$prefix §aStarted highlighting ${id}!")
                }
            }
        }
    }

    private fun getTargetEntity(): Entity? {
        val crosshairTarget = client.hitResult ?: return null
        return if (crosshairTarget is EntityHitResult) crosshairTarget.entity else null
    }
}
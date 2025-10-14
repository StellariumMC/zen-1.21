package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection.sbMobID
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.MouseEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.ui.ConfigMenuManager
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.entity.Entity
import net.minecraft.util.hit.EntityHitResult
import java.awt.Color

@Zen.Module
object BestiaryMobHighlight : Feature("bestiarymobhighlighter", true) {
    private val trackedMobs = mutableListOf<String>()
    private val highlightcolor by ConfigDelegate<Color>("bestiarymobhighlightcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Bestiary Mob Highlight", "Middle click on a mob in the world to toggle highlighting for it", "Visuals", xyz.meowing.zen.ui.ConfigElement(
                "bestiarymobhighlighter",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Highlight Color", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "bestiarymobhighlightcolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))

        return configUI
    }

    override fun initialize() {
        register<RenderEvent.EntityGlow> { event ->
            val mob = event.entity.sbMobID ?: return@register
            if (trackedMobs.contains(mob)) {
                event.shouldGlow = true
                event.glowColor = highlightcolor.toColorInt()
            }
        }

        register<MouseEvent.Click> { event ->
            if (event.button == 2) {
                val mob = getTargetEntity() ?: return@register
                val id = mob.sbMobID ?: return@register ChatUtils.addMessage("${Zen.Companion.prefix} §cThis mob could not be identified for the bestiary tracker!")
                if (trackedMobs.contains(id)) {
                    trackedMobs.remove(id)
                    ChatUtils.addMessage("${Zen.Companion.prefix} §cStopped highlighting ${id}!")
                } else {
                    trackedMobs.add(id)
                    ChatUtils.addMessage("${Zen.Companion.prefix} §aStarted highlighting ${id}!")
                }
            }
        }
    }

    private fun getTargetEntity(): Entity? {
        val crosshairTarget = mc.crosshairTarget ?: return null
        return if (crosshairTarget is EntityHitResult) crosshairTarget.entity else null
    }
}
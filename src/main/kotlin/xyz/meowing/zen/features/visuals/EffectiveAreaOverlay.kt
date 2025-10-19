package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.ui.ConfigMenuManager
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import xyz.meowing.zen.utils.ChatUtils
import java.awt.Color

@Zen.Module
object EffectiveAreaOverlay : Feature("effectiveareaoverlay", true) {
    private val items = listOf(
        "BAT_WAND",
        "STARRED_BAT_WAND",
        "HYPERION",
        "ASTRAEA",
        "SCYLLA",
        "VALKYRIE"
    )
    private val effectiveareaoverlaycolor by ConfigDelegate<Color>("effectiveareaoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigMenuManager
            .addFeature("Effective Area Overlay", "", "Visuals", xyz.meowing.zen.ui.ConfigElement(
                "effectiveareaoverlay",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "effectiveareaoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))

        return configUI
    }

    override fun initialize() {
        register<RenderEvent.World> { event ->
            val held = player?.mainHandStack?.skyblockID ?: return@register
            if (held in items) {
                val lookingAt = player?.raycast(if (held == "BAT_WAND" || held == "STARRED_BAT_WAND") 45.0 else 9.0, Utils.partialTicks, false) ?: return@register
                if (lookingAt.type == HitResult.Type.BLOCK) {
                    val blockHit = lookingAt as BlockHitResult
                    Render3D.drawFilledCircle(
                        event.consumers,
                        event.matrixStack,
                        Vec3d(blockHit.blockPos.x + 0.5, blockHit.blockPos.y + 1.0, blockHit.blockPos.z + 0.5),
                        7f,
                        72,
                        effectiveareaoverlaycolor.darker().toColorInt(),
                        effectiveareaoverlaycolor.toColorInt(),
                    )
                }
            }
        }
    }
}
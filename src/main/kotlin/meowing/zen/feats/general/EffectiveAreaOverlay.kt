package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.Render3D
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import java.awt.Color

@Zen.Module
object EffectiveAreaOverlay : Feature("effectiveareaoverlay") {
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
        return configUI
            .addElement("General", "Effective Area Overlay", ConfigElement(
                "effectiveareaoverlay",
                "Effective Area Overlay",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Effective Area Overlay", "Color",ConfigElement(
                "effectiveareaoverlaycolor",
                "Colorpicker",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<RenderEvent.World> { event ->
            val held = player?.mainHandStack?.skyblockID ?: return@register
            if (held in items) {
                val lookingAt = player?.raycast(if (held == "BAT_WAND" || held == "STARRED_BAT_WAND") 45.0 else 9.0, Utils.partialTicks, false) ?: return@register
                if (lookingAt.type == HitResult.Type.BLOCK) {
                    val blockHit = lookingAt as BlockHitResult
                    Render3D.drawFilledCircle(
                        event.context!!,
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
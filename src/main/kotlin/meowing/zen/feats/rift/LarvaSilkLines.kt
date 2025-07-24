package meowing.zen.feats.rift

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ItemUtils.isHolding
import meowing.zen.utils.Render3D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color

@Zen.Module
object LarvaSilkLines : Feature("larvasilklines", area = "the rift") {
    private var startingSilkPos: BlockPos? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Rift", "Larva silk", ConfigElement(
                "larvasilklines",
                "Larva silk lines display",
                "Displays the path of the larva silk line.",
                ElementType.Switch(false)
            ))
            .addElement("Rift", "Larva silk", ConfigElement(
                "larvasilklinescolor",
                "Colorpicker",
                "Color for larva silk display.",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting().startsWith("You cancelled the wire")) startingSilkPos = null
        }

        register<RenderEvent.World> { event ->
            if (startingSilkPos == null) return@register
            if (isHolding("LARVA_SILK") && event.context != null) {
                val lookingAt = mc.crosshairTarget
                Render3D.drawSpecialBB(startingSilkPos!!, config.larvasilklinescolor, event.context)

                if (lookingAt is BlockHitResult && lookingAt.type == HitResult.Type.BLOCK) {
                    val pos = startingSilkPos!!
                    val lookingAtPos = lookingAt.blockPos
                    val start = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
                    val finish = Vec3d(lookingAtPos.x + 0.5, lookingAtPos.y + 0.5, lookingAtPos.z + 0.5)

                    Render3D.drawLine(start, finish, 2f, config.larvasilklinescolor, event.context)
                    Render3D.drawSpecialBB(lookingAtPos, config.larvasilklinescolor, event.context)
                }
            }
        }

        register<EntityEvent.Interact> { event ->
            if (event.action == "USE_BLOCK" && isHolding("LARVA_SILK")) {
                if (startingSilkPos == null) {
                    startingSilkPos = event.pos
                    return@register
                }
                startingSilkPos = null
            }
        }

        register<WorldEvent.Change> {
            startingSilkPos = null
        }
    }
}
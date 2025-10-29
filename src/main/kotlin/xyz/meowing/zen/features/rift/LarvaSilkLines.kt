package xyz.meowing.zen.features.rift

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.isHolding
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import java.awt.Color

@Zen.Module
object LarvaSilkLines : Feature("larvasilklines", area = "the rift") {
    private var startingSilkPos: BlockPos? = null
    private val larvasilklinescolor by ConfigDelegate<Color>("larvasilklinescolor")

    override fun addConfig() {
        ConfigManager
            .addFeature("Larva silk display", "Larva silk lines display", "Rift", ConfigElement(
                "larvasilklines",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", ConfigElement(
                "larvasilklinescolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }


    override fun initialize() {
        createCustomEvent<RenderEvent.World>("render") { event ->
            if (startingSilkPos == null) return@createCustomEvent

            if (isHolding("LARVA_SILK")) {
                val consumers = event.context.consumers()
                val matrixStack = event.context.matrixStack()
                val lookingAt = client.crosshairTarget
                Render3D.drawSpecialBB(startingSilkPos!!, larvasilklinescolor, consumers, matrixStack)

                if (lookingAt is BlockHitResult && lookingAt.type == HitResult.Type.BLOCK) {
                    val pos = startingSilkPos!!
                    val lookingAtPos = lookingAt.blockPos
                    val start = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
                    val finish = Vec3d(lookingAtPos.x + 0.5, lookingAtPos.y + 0.5, lookingAtPos.z + 0.5)

                    Render3D.drawLine(start, finish, 2f, larvasilklinescolor, consumers, matrixStack)
                    Render3D.drawSpecialBB(lookingAtPos, larvasilklinescolor, consumers, matrixStack)
                }
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting().startsWith("You cancelled the wire")) {
                startingSilkPos = null
                unregisterEvent("render")
            }
        }

        register<EntityEvent.Interact> { event ->
            if (event.action == "USE_BLOCK" && isHolding("LARVA_SILK")) {
                if (startingSilkPos == null) {
                    startingSilkPos = event.pos
                    registerEvent("render")
                    return@register
                }
                startingSilkPos = null
                unregisterEvent("render")
            }
        }

        register<WorldEvent.Change> {
            startingSilkPos = null
            unregisterEvent("render")
        }
    }
}
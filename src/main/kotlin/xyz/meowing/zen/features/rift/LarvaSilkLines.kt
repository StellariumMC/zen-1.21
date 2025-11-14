package xyz.meowing.zen.features.rift

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.isHolding
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color

@Module
object LarvaSilkLines : Feature(
    "larvaSilkLines",
    island = SkyBlockIsland.THE_RIFT
) {
    private var startingSilkPos: BlockPos? = null
    private val larvaSilkLinesColor by ConfigDelegate<Color>("larvaSilkLines.color")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Larva Silk display",
                "Larva silk lines display",
                "Rift",
                ConfigElement(
                    "larvaSilkLines",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Color",
                ConfigElement(
                    "larvaSilkLines.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }


    override fun initialize() {
        createCustomEvent<RenderEvent.World.Last>("render") { event ->
            if (startingSilkPos == null) return@createCustomEvent

            if (isHolding("LARVA_SILK")) {
                val consumers = event.context.consumers()
                val matrixStack = event.context.matrixStack()
                val lookingAt = client.hitResult
                Render3D.drawSpecialBB(startingSilkPos!!, larvaSilkLinesColor, consumers, matrixStack)

                if (lookingAt is BlockHitResult && lookingAt.type == HitResult.Type.BLOCK) {
                    val pos = startingSilkPos!!
                    val lookingAtPos = lookingAt.blockPos
                    val start = Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
                    val finish = Vec3(lookingAtPos.x + 0.5, lookingAtPos.y + 0.5, lookingAtPos.z + 0.5)

                    Render3D.drawLine(start, finish, 2f, larvaSilkLinesColor, consumers, matrixStack)
                    Render3D.drawSpecialBB(lookingAtPos, larvaSilkLinesColor, consumers, matrixStack)
                }
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register

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

        register<LocationEvent.WorldChange> {
            startingSilkPos = null
            unregisterEvent("render")
        }
    }
}
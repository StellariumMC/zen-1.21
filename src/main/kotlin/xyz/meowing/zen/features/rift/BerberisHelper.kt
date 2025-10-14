package xyz.meowing.zen.features.rift

//#if MC < 1.21.9
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.PacketEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexRendering
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import java.awt.Color
import kotlin.math.hypot

@Zen.Module
object BerberisHelper : Feature("berberishelper", area = "the rift", subarea =  "dreadfarm") {
    private var blockPos: BlockPos? = null
    private val berberishelpercolor by ConfigDelegate<Color>("berberishelpercolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Berberis Helper", "Berberis highlight", "Rift", xyz.meowing.zen.ui.ConfigElement(
                "berberishelper",
                ElementType.Switch(false)
            ))
            .addFeatureOption("", "Color", "", xyz.meowing.zen.ui.ConfigElement(
                "berberishelpercolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
        return configUI
    }


    override fun initialize() {
        register<PacketEvent.Received> { event ->
            val packet = event.packet as? ParticleS2CPacket ?: return@register
            if (packet.parameters.type != ParticleTypes.FIREWORK) return@register
            val playerX = player?.x ?: return@register
            val playerZ = player?.z ?: return@register
            if (hypot(playerX - packet.x, playerZ - packet.z) > 20) return@register

            val pos = BlockPos(packet.x.toInt() - 1, packet.y.toInt(), packet.z.toInt() - 1)
            val below = BlockPos(packet.x.toInt() - 1, packet.y.toInt() - 1, packet.z.toInt() - 1)

            if (world?.getBlockState(pos)?.block == Blocks.DEAD_BUSH && world?.getBlockState(below)?.block == Blocks.FARMLAND) {
                blockPos = pos
            }
        }

        register<RenderEvent.World> { event ->
            val targetPos = blockPos ?: return@register
            val consumers = event.context?.consumers() ?: return@register
            val blockState = world?.getBlockState(targetPos) ?: return@register
            val camera = mc.gameRenderer.camera
            val blockShape = blockState.getOutlineShape(world, targetPos, ShapeContext.of(camera.focusedEntity))
            if (blockShape.isEmpty) return@register

            val camPos = camera.pos
            VertexRendering.drawOutline(
                event.context.matrixStack(),
                consumers.getBuffer(RenderLayer.getLines()),
                blockShape,
                targetPos.x - camPos.x,
                targetPos.y - camPos.y,
                targetPos.z - camPos.z,
                berberishelpercolor.toColorInt()
            )
        }
    }
}
//#endif
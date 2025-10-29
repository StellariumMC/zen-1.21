package xyz.meowing.zen.features.rift

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
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
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.config.ConfigElement
import xyz.meowing.zen.config.ConfigManager
import java.awt.Color
import kotlin.math.hypot

@Zen.Module
object BerberisHelper : Feature("berberishelper", area = "the rift", subarea =  "dreadfarm") {
    private var blockPos: BlockPos? = null
    private val berberishelpercolor by ConfigDelegate<Color>("berberishelpercolor")

    override fun addConfig() {
        ConfigManager
            .addFeature("Berberis Helper", "Berberis highlight", "Rift", ConfigElement(
                "berberishelper",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", ConfigElement(
                "berberishelpercolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
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
            val consumers = event.context.consumers()
            val blockState = world?.getBlockState(targetPos) ?: return@register
            val camera = client.gameRenderer.camera
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
package xyz.meowing.zen.features.rift

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.BlockPos
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockAreas
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.PacketEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color
import kotlin.math.hypot

@Module
object BerberisHelper : Feature(
    "berberishelper",
    island = SkyBlockIsland.THE_RIFT,
    area = SkyBlockAreas.DREADFARM
) {
    private var blockPos: BlockPos? = null
    private val berberisHelperColor by ConfigDelegate<Color>("berberisHelper.color")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Berberis helper",
                "Berberis highlight",
                "Rift",
                ConfigElement(
                    "berberisHelper",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Color",
                ConfigElement(
                    "berberisHelper.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }


    override fun initialize() {
        register<PacketEvent.Received> { event ->
            val packet = event.packet as? ClientboundLevelParticlesPacket ?: return@register
            if (packet.particle.type != ParticleTypes.FIREWORK) return@register
            val playerX = player?.x ?: return@register
            val playerZ = player?.z ?: return@register
            if (hypot(playerX - packet.x, playerZ - packet.z) > 20) return@register

            val pos = BlockPos(packet.x.toInt() - 1, packet.y.toInt(), packet.z.toInt() - 1)
            val below = BlockPos(packet.x.toInt() - 1, packet.y.toInt() - 1, packet.z.toInt() - 1)

            if (world?.getBlockState(pos)?.block == Blocks.DEAD_BUSH && world?.getBlockState(below)?.block == Blocks.FARMLAND) {
                blockPos = pos
            }
        }

        register<RenderEvent.World.Last> { event ->
            val targetPos = blockPos ?: return@register
            val consumers = event.context.consumers()
            val blockState = world?.getBlockState(targetPos) ?: return@register
            val camera = client.gameRenderer.mainCamera
            val blockShape = blockState.getShape(world, targetPos, CollisionContext.of(camera.entity))
            if (blockShape.isEmpty) return@register

            val camPos = camera.position
            ShapeRenderer.renderShape(
                event.context.matrixStack(),
                consumers.getBuffer(RenderType.lines()),
                blockShape,
                targetPos.x - camPos.x,
                targetPos.y - camPos.y,
                targetPos.z - camPos.z,
                berberisHelperColor.rgb
            )
        }
    }
}
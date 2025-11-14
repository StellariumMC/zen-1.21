package xyz.meowing.zen.features.visuals

import net.minecraft.world.phys.shapes.CollisionContext
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.EmptyBlockGetter
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color

@Module
object EffectiveAreaOverlay : Feature(
    "effectiveAreaOverlay",
    true
) {
    private val items = listOf(
        "BAT_WAND",
        "STARRED_BAT_WAND",
        "HYPERION",
        "ASTRAEA",
        "SCYLLA",
        "VALKYRIE"
    )
    private val color by ConfigDelegate<Color>("effectiveAreaOverlay.color")
    private val renderMethod by ConfigDelegate<Int>("effectiveAreaOverlay.renderMethod")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Effective area overlay",
                "Shows your effective farming area",
                "Visuals",
                ConfigElement(
                    "effectiveAreaOverlay",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Color",
                ConfigElement(
                    "effectiveAreaOverlay.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
            .addFeatureOption(
                "Render method",
                ConfigElement(
                    "effectiveAreaOverlay.renderMethod",
                    ElementType.Dropdown(listOf("Circle", "Blocks"), 0)
                )
            )
    }

    var lastBlockHit: BlockPos? = null
    val cachedBlockShapes = mutableSetOf<Any>()

    override fun initialize() {
        register<RenderEvent.World.Last> { event ->
            val held = player?.mainHandItem?.skyblockID ?: return@register
            if (held in items) {
                val lookingAt = player?.pick(if (held == "BAT_WAND" || held == "STARRED_BAT_WAND") 45.0 else 9.0, Utils.partialTicks, false) ?: return@register
                if (lookingAt.type == HitResult.Type.BLOCK) {
                    val blockHit = lookingAt as BlockHitResult

                    when (renderMethod) {
                        0 -> {
                            Render3D.drawFilledCircle(
                                event.context.consumers(),
                                event.context.matrixStack(),
                                Vec3(blockHit.blockPos.x + 0.5, blockHit.blockPos.y + 1.0, blockHit.blockPos.z + 0.5),
                                7f,
                                72,
                                color.darker().rgb,
                                color.rgb,
                            )
                        }
                        1 -> {
                            val camera = client.gameRenderer.mainCamera
                            val radius = 6
                            val center = blockHit.blockPos

                            if(lastBlockHit != blockHit.blockPos) {
                                cachedBlockShapes.clear()
                                xLoop@ for (x in -radius..radius) {
                                    yLoop@ for (y in -radius..radius) {
                                        zLoop@ for (z in -radius..radius) {
                                            val distance = Math.sqrt((x * x + y * y + z * z).toDouble())

                                            // Only include blocks near the sphere surface
                                            if (distance < radius - 0.5 || distance > radius + 0.5) continue@zLoop

                                            val blockPos = center.offset(x, y, z)
                                            val blockState = KnitClient.world?.getBlockState(blockPos) ?: continue@zLoop

                                            // Ignore plants
                                            if (blockState.block is net.minecraft.world.level.block.VegetationBlock) continue@zLoop

                                            val blockShape = blockState.getShape(
                                                EmptyBlockGetter.INSTANCE,
                                                blockPos,
                                                CollisionContext.of(camera.entity)
                                            )
                                            if (blockShape.isEmpty) continue@zLoop

                                            cachedBlockShapes.add(blockShape.move(blockPos))
                                        }
                                    }
                                }
                            }
                            lastBlockHit = blockHit.blockPos

                            cachedBlockShapes.forEach {
                                Render3D.drawFilledShapeVoxel(
                                    it as VoxelShape,
                                    color,
                                    event.context.consumers(),
                                    event.context.matrixStack()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

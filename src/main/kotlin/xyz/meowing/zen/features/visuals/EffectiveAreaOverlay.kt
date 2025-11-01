package xyz.meowing.zen.features.visuals

import net.minecraft.block.ShapeContext
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.EmptyBlockView
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import java.awt.Color

@Module
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
    private val renderMethod by ConfigDelegate<Int>("renderMethod")

    override fun addConfig() {
        ConfigManager
            .addFeature("Effective Area Overlay", "", "Visuals", ConfigElement(
                "effectiveareaoverlay",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Color", "", "Options", ConfigElement(
                "effectiveareaoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addFeatureOption("Render Method", "Choose the rendering method", "Options", ConfigElement(
                "renderMethod",
                ElementType.Dropdown(listOf("Circle", "Blocks"), 0)
            ))
    }

    var lastBlockHit: BlockPos? = null
    val cachedBlockShapes = mutableSetOf<Any>()

    override fun initialize() {
        register<RenderEvent.World.Last> { event ->
            val held = player?.mainHandStack?.skyblockID ?: return@register
            if (held in items) {
                val lookingAt = player?.raycast(if (held == "BAT_WAND" || held == "STARRED_BAT_WAND") 45.0 else 9.0, Utils.partialTicks, false) ?: return@register
                if (lookingAt.type == HitResult.Type.BLOCK) {
                    val blockHit = lookingAt as BlockHitResult

                    when (renderMethod) {
                        0 -> {
                            Render3D.drawFilledCircle(
                                event.context.consumers(),
                                event.context.matrixStack(),
                                Vec3d(blockHit.blockPos.x + 0.5, blockHit.blockPos.y + 1.0, blockHit.blockPos.z + 0.5),
                                7f,
                                72,
                                effectiveareaoverlaycolor.darker().toColorInt(),
                                effectiveareaoverlaycolor.toColorInt(),
                            )
                        }
                        1 -> {
                            val camera = client.gameRenderer.camera
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

                                            val blockPos = center.add(x, y, z)
                                            val blockState = KnitClient.world?.getBlockState(blockPos) ?: continue@zLoop

                                            // Ignore plants
                                            if (blockState.block is net.minecraft.block.PlantBlock) continue@zLoop

                                            val blockShape = blockState.getOutlineShape(
                                                EmptyBlockView.INSTANCE,
                                                blockPos,
                                                ShapeContext.of(camera.focusedEntity)
                                            )
                                            if (blockShape.isEmpty) continue@zLoop

                                            cachedBlockShapes.add(blockShape.offset(blockPos))
                                        }
                                    }
                                }
                            }
                            lastBlockHit = blockHit.blockPos

                            cachedBlockShapes.forEach {
                                Render3D.drawFilledShapeVoxel(
                                    it as VoxelShape,
                                    effectiveareaoverlaycolor,
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

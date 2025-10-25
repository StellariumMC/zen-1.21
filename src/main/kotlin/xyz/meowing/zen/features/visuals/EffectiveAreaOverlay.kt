package xyz.meowing.zen.features.visuals

import net.minecraft.block.ShapeContext
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.toColorInt
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EmptyBlockView
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.config.ConfigElement
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

    override fun initialize() {
        register<RenderEvent.World> { event ->
            val held = player?.mainHandStack?.skyblockID ?: return@register
            if (held in items) {
                val lookingAt = player?.raycast(if (held == "BAT_WAND" || held == "STARRED_BAT_WAND") 45.0 else 9.0, Utils.partialTicks, false) ?: return@register
                if (lookingAt.type == HitResult.Type.BLOCK) {
                    val blockHit = lookingAt as BlockHitResult

                    when (renderMethod) {
                        0 -> {
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
                        1 -> {
                            val radius = 6
                            val center = blockHit.blockPos
                            val camera = client.gameRenderer.camera

                            // Not sure if this is the most efficient but I can't think of a better way rn
                            // Has an error with certain areas for some reason, will just not render after a certain x or z value but then works again after further away
                            // 51 70 -108 is an example coord for the hub that causes issues
                            for (x in -radius..radius) {
                                for (y in -radius..radius) {
                                    for (z in -radius..radius) {
                                        val blockPos = center.add(x, y, z)
                                        val blockState = KnitClient.world?.getBlockState(blockPos) ?: continue
                                        val distance = Math.sqrt((x * x + y * y + z * z).toDouble())
                                        if (distance <= radius && !blockState.isAir) {
                                            val blockShape = blockState.getOutlineShape(EmptyBlockView.INSTANCE, blockPos, ShapeContext.of(camera.focusedEntity))
                                            if (blockShape.isEmpty) return@register

                                            Render3D.drawFilledShapeVoxel(
                                                blockShape.offset(blockPos),
                                                effectiveareaoverlaycolor,
                                                event.consumers,
                                                event.matrixStack
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package meowing.zen.features.meowing

//#if MC < 1.21.9
import meowing.zen.Zen
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.NetworkUtils
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.BlockModelRenderer
import net.minecraft.client.render.model.BlockStateManagers
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import java.io.File

@Zen.Module
object Rat : Feature(area = "Hub") {
    private val position = Vec3d(-1.0, 72.0, -92.0)
    private val culling = Box(position.x, position.y, position.z, position.x + 1, position.y + 1, position.z + 1/16.0)
    private val textureId = Identifier.of("zen", "zen_rat_png")
    private var textureLoaded = false

    override fun initialize() {
        loadTexture()
        register<RenderEvent.WorldPostEntities> { event ->
            if (textureLoaded) {
                render(event.context!!)
            }
        }
    }

    private fun loadTexture() {
        val cacheFile = File(mc.runDirectory, "cache/zen_rat.png")
        cacheFile.parentFile.mkdirs()

        NetworkUtils.downloadFile(
            url = "https://github.com/meowing-xyz/zen-data/raw/main/assets/rat.png",
            outputFile = cacheFile,
            onComplete = { file ->
                mc.execute {
                    try {
                        val image = NativeImage.read(file.inputStream())
                        val texture = NativeImageBackedTexture({ "zen_rat" }, image)
                        mc.textureManager.registerTexture(textureId, texture)
                        textureLoaded = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onError = { error ->
                error.printStackTrace()
            }
        )
    }

    private fun render(context: WorldRenderContext) {
        val cameraPos = context.camera().pos
        val frustum = context.frustum()

        if (position.distanceTo(cameraPos) > 96.0) return
        if (frustum != null && !frustum.isVisible(culling)) return

        val itemFrameState = BlockStateManagers.getStateForItemFrame(false, true)
        val blockModel = mc.blockRenderManager.getModel(itemFrameState)
        val consumers = context.consumers() ?: return
        val matrices = context.matrixStack() ?: return

        matrices.push()
        matrices.translate(
            position.x - cameraPos.x + 1.0,
            position.y - cameraPos.y,
            position.z - cameraPos.z + 1.0
        )
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))

        val entry = matrices.peek()
        val blockBuffer = consumers.getBuffer(
            RenderLayer.getEntitySolidZOffsetForward(Identifier.of("minecraft", "textures/atlas/blocks.png"))
        )

        BlockModelRenderer.render(
            entry,
            blockBuffer,
            blockModel,
            1.0f, 1.0f, 1.0f,
            15,
            OverlayTexture.DEFAULT_UV
        )

        matrices.translate(1.0f, 1.0f, 0.0f)
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f))

        val posMatrix = entry.positionMatrix
        val overlayBuffer = consumers.getBuffer(RenderLayer.getText(textureId))
        val depth = 0.9375f - 0.000488f

        overlayBuffer.vertex(posMatrix, 0f, 1f, depth).color(-1).texture(0f, 1f).light(15)
        overlayBuffer.vertex(posMatrix, 1f, 1f, depth).color(-1).texture(1f, 1f).light(15)
        overlayBuffer.vertex(posMatrix, 1f, 0f, depth).color(-1).texture(1f, 0f).light(15)
        overlayBuffer.vertex(posMatrix, 0f, 0f, depth).color(-1).texture(0f, 0f).light(15)

        matrices.pop()
    }
}
//#endif
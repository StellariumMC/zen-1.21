package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.NetworkUtils
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.ModelBlockRenderer
import net.minecraft.client.resources.model.BlockStateDefinitions
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.AABB
import com.mojang.math.Axis
import net.minecraft.world.phys.Vec3
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.render.world.RenderContext
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.RenderEvent

import java.io.File

@Module
object Rat : Feature(
    island = SkyBlockIsland.HUB
) {
    private val position = Vec3(-1.0, 72.0, -92.0)
    private val culling = AABB(position.x, position.y, position.z, position.x + 1, position.y + 1, position.z + 1/16.0)
    private val textureId = ResourceLocation.fromNamespaceAndPath("zen", "zen_rat_png")
    private var textureLoaded = false

    override fun initialize() {
        loadTexture()

        register<RenderEvent.World.AfterEntities> { event ->
            if (textureLoaded) {
                render(event.context)
            }
        }
    }

    private fun loadTexture() {
        val cacheFile = File(client.gameDirectory, "cache/zen_rat.png")
        cacheFile.parentFile.mkdirs()

        NetworkUtils.downloadFile(
            url = "https://github.com/meowing-xyz/zen-data/raw/main/assets/rat.png",
            outputFile = cacheFile,
            onComplete = { file ->
                client.execute {
                    try {
                        val image = NativeImage.read(file.inputStream())
                        val texture = DynamicTexture({ "zen_rat" }, image)
                        client.textureManager.register(textureId, texture)
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

    private fun render(context: RenderContext) {
        val camera = client.gameRenderer.mainCamera
        val cameraPos = camera.position

        val frustum = context.frustum() ?: return

        if (position.distanceTo(cameraPos) > 96.0) return
        if (!frustum.isVisible(culling)) return

        val itemFrameState = BlockStateDefinitions.getItemFrameFakeState(false, true)
        val blockModel = client.blockRenderer.getBlockModel(itemFrameState)
        val consumers = context.consumers()
        val matrices = context.matrixStack() ?: return

        matrices.pushPose()
        matrices.translate(
            position.x - cameraPos.x + 1.0,
            position.y - cameraPos.y,
            position.z - cameraPos.z + 1.0
        )
        matrices.mulPose(Axis.YP.rotationDegrees(180f))

        val entry = matrices.last()
        val blockBuffer = consumers.getBuffer(
            RenderType.entitySolidZOffsetForward(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"))
        )

        ModelBlockRenderer.renderModel(
            entry,
            blockBuffer,
            blockModel,
            1.0f, 1.0f, 1.0f,
            15,
            OverlayTexture.NO_OVERLAY
        )

        matrices.translate(1.0f, 1.0f, 0.0f)
        matrices.mulPose(Axis.ZP.rotationDegrees(180f))

        val posMatrix = entry.pose()
        val overlayBuffer = consumers.getBuffer(RenderType.text(textureId))
        val depth = 0.9375f - 0.000488f

        overlayBuffer.addVertex(posMatrix, 0f, 1f, depth).setColor(-1).setUv(0f, 1f).setLight(15)
        overlayBuffer.addVertex(posMatrix, 1f, 1f, depth).setColor(-1).setUv(1f, 1f).setLight(15)
        overlayBuffer.addVertex(posMatrix, 1f, 0f, depth).setColor(-1).setUv(1f, 0f).setLight(15)
        overlayBuffer.addVertex(posMatrix, 0f, 0f, depth).setColor(-1).setUv(0f, 0f).setLight(15)

        matrices.popPose()
    }
}
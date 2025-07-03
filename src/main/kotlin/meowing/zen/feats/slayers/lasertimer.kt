package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.toColorInt
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.BufferAllocator
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import java.awt.Color

object lasertimer : Feature("lasertimer") {
    private var bossID = 0
    private val totaltime = 8.2 // laser time I think?
    private val renderCall: EventBus.EventCall = EventBus.register<RenderEvent.WorldPostEntities> ({ renderString() }, false)

    override fun initialize() {
        register<EntityEvent.Leave> { event ->
            if (event.entity.id == bossID) {
                bossID = 0
                renderCall.unregister()
            }
        }
    }

    fun handleSpawn(entityID: Int) {
        bossID = entityID - 3
        renderCall.register()
    }

    fun renderString() {
        val ent = mc.world?.getEntityById(bossID) ?: return
        val ridingentity = ent.vehicle ?: return
        val time = maxOf(0.0, totaltime - (ridingentity.age / 20.0))
        val pos = ent.pos
        val camera = mc.gameRenderer.camera
        val cameraPos = camera.pos
        val allocator = BufferAllocator(256)
        val consumers = VertexConsumerProvider.immediate(allocator)

        val text = "§bLaser: §c${"%.1f".format(time)}"
        val positionMatrix = Matrix4f()
            .translate(
                (pos.x - cameraPos.x).toFloat(),
                ((pos.y - cameraPos.y) + 1).toFloat(),
                (pos.z - cameraPos.z).toFloat()
            )
            .rotate(camera.rotation)
            .scale(0.05f, -0.05f, 0.05f)

        val xOffset = -mc.textRenderer.getWidth(text) / 2f
        val depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC)
        val depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_ALWAYS)

        mc.textRenderer.draw(
            text,
            xOffset,
            0f,
            Color(255, 255, 255).toColorInt(),
            false,
            positionMatrix,
            consumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )

        consumers.draw()

        GL11.glDepthFunc(depthFunc)
        if (!depthTest) GL11.glDisable(GL11.GL_DEPTH_TEST)
    }
}
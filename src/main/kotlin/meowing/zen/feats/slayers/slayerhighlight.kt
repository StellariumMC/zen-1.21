package meowing.zen.feats.slayers

import meowing.zen.utils.EventBus
import meowing.zen.utils.EventTypes
import net.minecraft.client.MinecraftClient
import meowing.zen.featManager
import meowing.zen.utils.utils
import meowing.zen.feats.slayers.slayertimer.Companion.BossId
import meowing.zen.feats.slayers.slayertimer.Companion.isFighting

class slayerhighlight private constructor() {
    companion object {
        private val instance = slayerhighlight()

        @JvmStatic
        fun initialize() {
            featManager.register(instance) {
                EventBus.register(EventTypes.WorldRenderEvent::class.java, instance, instance::handleWorldRender)
            }
        }
    }

    private fun handleWorldRender(event: EventTypes.WorldRenderEvent) {
        if (!isFighting || BossId == -1) return

        val client = MinecraftClient.getInstance()
        val bossEntity = client.world?.getEntityById(BossId) ?: return

        val cameraPos = event.context.camera().pos
        val entityPos = bossEntity.getLerpedPos(event.getTickDelta())
        val x = entityPos.x - cameraPos.x
        val y = entityPos.y - cameraPos.y
        val z = entityPos.z - cameraPos.z

        val width = (bossEntity.width + 0.5).toFloat()
        val height = (bossEntity.height + 0.25).toFloat()
        utils.renderEntityBox(event.context.matrixStack(), event.context.consumers(), x, y, z, width, height, 0f, 1f, 1f, 0.5f)
    }
}
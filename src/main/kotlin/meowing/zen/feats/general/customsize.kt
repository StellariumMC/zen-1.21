package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature

object customsize : Feature("customsize") {
    private var customX = 1.0f
    private var customY = 1.0f
    private var customZ = 1.0f
    private var customself = false

    override fun initialize() {
        Zen.registerCallback("customX") { newval ->
            customX = (newval as Double).toFloat()
        }
        Zen.registerCallback("customY") { newval ->
            customY = (newval as Double).toFloat()
        }
        Zen.registerCallback("customZ") { newval ->
            customZ = (newval as Double).toFloat()
        }
        Zen.registerCallback("customself") { newval ->
            customself = newval as Boolean
        }

        register<RenderEvent.PlayerPre> { event ->
            if (!customself || event.entity.id == mc.player?.id) event.matrices.scale(customX, customY, customZ)
        }
    }
}
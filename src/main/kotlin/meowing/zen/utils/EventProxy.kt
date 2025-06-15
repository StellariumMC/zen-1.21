package meowing.zen.utils

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object EventProxy {
    @JvmStatic
    fun initialize() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { msg, overlay ->
            val event = EventTypes.GameMessageEvent(msg, overlay)
            EventBus.fire(event)
            !event.isHidden()
        }
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, world -> EventBus.fire(EventTypes.EntityUnloadEvent(entity, world)) }
        ClientEntityEvents.ENTITY_LOAD.register { entity, world -> EventBus.fire(EventTypes.EntityLoadEvent(entity, world)) }
        ClientTickEvents.END_CLIENT_TICK.register { client -> EventBus.fire(EventTypes.ClientTickEvent(client)) }
        WorldRenderEvents.AFTER_ENTITIES.register { context -> EventBus.fire(EventTypes.WorldRenderEvent(context)) }
    }
}
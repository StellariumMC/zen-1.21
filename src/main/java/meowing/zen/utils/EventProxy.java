package meowing.zen.utils;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import meowing.zen.utils.EventTypes.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class EventProxy {
    public static void initialize() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            var event = new GameMessageEvent(msg, overlay);
            EventBus.fire(event);
            return !event.hide;
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> 
            EventBus.fire(new EntityUnloadEvent(entity, world)));
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> 
            EventBus.fire(new EntityLoadEvent(entity, world)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> 
            EventBus.fire(new ClientTickEvent(client)));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> 
            EventBus.fire(new WorldRenderEvent(context)));
    }
}
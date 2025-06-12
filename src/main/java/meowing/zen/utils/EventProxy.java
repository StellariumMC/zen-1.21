package meowing.zen.utils;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import meowing.zen.utils.EventTypes.GameMessageEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EventProxy {
    public static void initialize() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(EventProxy::handleGameMessage);
        ClientEntityEvents.ENTITY_UNLOAD.register(EventProxy::handleEntityUnload);
        ClientEntityEvents.ENTITY_LOAD.register(EventProxy::handleEntityLoad);
        ClientTickEvents.END_CLIENT_TICK.register(EventProxy::handleClientTick);
        WorldRenderEvents.AFTER_ENTITIES.register(EventProxy::handleWorldRender);
    }

    private static boolean handleGameMessage(Text message, boolean overlay) {
        GameMessageEvent event = new GameMessageEvent(message, overlay);
        EventBus.fire(event);
        return !event.hide;
    }

    private static void handleEntityUnload(Entity entity, World world) {
        EventBus.fire(new EventTypes.EntityUnloadEvent(entity, world));
    }

    private static void handleEntityLoad(Entity entity, World world) {
        EventBus.fire(new EventTypes.EntityLoadEvent(entity, world));
    }

    private static void handleClientTick(MinecraftClient client) {
        EventBus.fire(new EventTypes.ClientTickEvent(client));
    }

    private static void handleWorldRender(WorldRenderContext context) {
        EventBus.fire(new EventTypes.WorldRenderEvent(context));
    }
}
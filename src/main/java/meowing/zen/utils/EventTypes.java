package meowing.zen.utils;

import net.minecraft.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public class EventTypes {
    public static class GameMessageEvent {
        public Text message;
        public boolean overlay;
        public boolean hide = false;
        
        public GameMessageEvent(Text message, boolean overlay) {
            this.message = message;
            this.overlay = overlay;
        }
    }
    
    public static class EntityUnloadEvent {
        public final Entity entity;
        public final World world;
        
        public EntityUnloadEvent(Entity entity, World world) {
            this.entity = entity;
            this.world = world;
        }
    }
    
    public static class EntityLoadEvent {
        public final Entity entity;
        public final World world;
        
        public EntityLoadEvent(Entity entity, World world) {
            this.entity = entity;
            this.world = world;
        }
    }
    
    public static class ClientTickEvent {
        public final MinecraftClient client;
        
        public ClientTickEvent(MinecraftClient client) {
            this.client = client;
        }
    }
    
    public static class WorldRenderEvent {
        public final WorldRenderContext context;
        
        public WorldRenderEvent(WorldRenderContext context) {
            this.context = context;
        }
    }
}
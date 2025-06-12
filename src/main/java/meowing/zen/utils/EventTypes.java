package meowing.zen.utils;

import net.minecraft.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import java.util.Objects;

public final class EventTypes {
    private EventTypes() {}
    
    public static final class GameMessageEvent {
        public final Text message;
        public final boolean overlay;
        public volatile boolean hide = false;
        
        public GameMessageEvent(Text message, boolean overlay) {
            this.message = Objects.requireNonNull(message, "Message cannot be null");
            this.overlay = overlay;
        }
        
        public void hide() {
            this.hide = true;
        }

        public void unhide() {
            this.hide = false;
        }

        public boolean isHidden() {
            return hide;
        }
        
        public String getPlainText() {
            return message.getString();
        }
    }
    
    public static class EntityEvent {
        public final Entity entity;
        public final World world;
        
        protected EntityEvent(Entity entity, World world) {
            this.entity = Objects.requireNonNull(entity, "Entity cannot be null");
            this.world = Objects.requireNonNull(world, "World cannot be null");
        }
        
        public boolean isPlayer() {
            return entity instanceof net.minecraft.entity.player.PlayerEntity;
        }
        
        public boolean isLiving() {
            return entity instanceof net.minecraft.entity.LivingEntity;
        }

        public Entity getEntity() {
            return entity;
        }

        public int getEntityId() {
            return entity.getId();
        }
    }
    
    public static final class EntityUnloadEvent extends EntityEvent {
        public EntityUnloadEvent(Entity entity, World world) {
            super(entity, world);
        }
    }
    
    public static final class EntityLoadEvent extends EntityEvent {
        public EntityLoadEvent(Entity entity, World world) {
            super(entity, world);
        }
    }
    
    public static final class ClientTickEvent {
        public final MinecraftClient client;
        
        public ClientTickEvent(MinecraftClient client) {
            this.client = Objects.requireNonNull(client, "Client cannot be null");
        }
        
        public boolean isInGame() {
            return client.world != null && client.player != null;
        }
        
        public boolean isPaused() {
            return client.isPaused();
        }
    }
    
    public static final class WorldRenderEvent {
        public final WorldRenderContext context;
        
        public WorldRenderEvent(WorldRenderContext context) {
            this.context = Objects.requireNonNull(context, "Context cannot be null");
        }
        
        public float getTickDelta() {
            return context.tickCounter().getTickProgress(false);
        }
        
        public MinecraftClient getClient() {
            return context.gameRenderer().getClient();
        }

        public WorldRenderContext getContext() {
            return context;
        }
    }
}
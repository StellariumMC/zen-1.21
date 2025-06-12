package meowing.zen.feats.slayers;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.Objects;
import meowing.zen.featManager;

import static meowing.zen.feats.slayers.slayertimer.isFighting;
import static meowing.zen.feats.slayers.slayertimer.BossId;

public class slayerhighlight {
    public static void initialize() {
        featManager.register(new slayerhighlight(), () -> {
            EventBus.register(EventTypes.WorldRenderEvent.class, slayerhighlight.class, slayerhighlight::handleWorldRender);
        });
    }

    private static void handleWorldRender(EventTypes.WorldRenderEvent event) {
        if (!isFighting || BossId == -1) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        Entity bossEntity = null;
        for (Entity entity : Objects.requireNonNull(client.world).getEntities()) {
            if (entity.getId() == BossId && isValidSlayerEntity(entity)) {
                bossEntity = entity;
                break;
            }
        }
        if (bossEntity == null) return;

        Vec3d cameraPos = event.context.camera().getPos();
        Vec3d entityPos = bossEntity.getLerpedPos(event.context.tickCounter().getTickProgress(false));
        double x = entityPos.x - cameraPos.x;
        double y = entityPos.y - cameraPos.y;
        double z = entityPos.z - cameraPos.z;

        float width = (float) (bossEntity.getWidth() + 0.5);
        float height = (float) (bossEntity.getHeight() + 0.25);
        renderEntityBox(event.context.matrixStack(), event.context.consumers(), x, y, z, width, height);
    }

    private static boolean isValidSlayerEntity(Entity entity) {
        return entity instanceof EndermanEntity || entity instanceof WolfEntity || 
               entity instanceof SpiderEntity || entity instanceof ZombieEntity || 
               entity instanceof BlazeEntity;
    }

    private static void renderEntityBox(MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                                       double x, double y, double z, float width, float height) {
        Box box = new Box(x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2);
        DebugRenderer.drawBox(matrices, vertexConsumers, box, 0.0f, 1.0f, 1.0f, 0.5f);
    }
}
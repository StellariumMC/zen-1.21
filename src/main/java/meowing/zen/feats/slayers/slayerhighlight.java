package meowing.zen.feats.slayers;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import meowing.zen.featManager;
import meowing.zen.utils.utils;
import static meowing.zen.feats.slayers.slayertimer.isFighting;
import static meowing.zen.feats.slayers.slayertimer.BossId;

public class slayerhighlight {
    private static final slayerhighlight instance = new slayerhighlight();
    private slayerhighlight() {}
    public static void initialize() {
        featManager.register(instance, () -> {
            EventBus.register(EventTypes.WorldRenderEvent.class, instance, instance::handleWorldRender);
        });
    }

    private void handleWorldRender(EventTypes.WorldRenderEvent event) {
        if (!isFighting || BossId == -1) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        Entity bossEntity = client.world.getEntityById(BossId);
        if (bossEntity == null) return;

        Vec3d cameraPos = event.getContext().camera().getPos();
        Vec3d entityPos = bossEntity.getLerpedPos(event.getTickDelta());
        double x = entityPos.x - cameraPos.x;
        double y = entityPos.y - cameraPos.y;
        double z = entityPos.z - cameraPos.z;

        float width = (float) (bossEntity.getWidth() + 0.5);
        float height = (float) (bossEntity.getHeight() + 0.25);
        utils.renderEntityBox(event.getContext().matrixStack(), event.getContext().consumers(), x, y, z, width, height, 0f, 1f, 1f, 0.5f);
    }
}
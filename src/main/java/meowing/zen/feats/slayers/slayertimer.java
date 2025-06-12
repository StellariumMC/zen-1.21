package meowing.zen.feats.slayers;

import meowing.zen.utils.EventBus;
import meowing.zen.utils.EventTypes;
import meowing.zen.utils.chatutils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.*;
import meowing.zen.featManager;
import meowing.zen.utils.TickScheduler;
import java.util.Objects;
import java.util.regex.Pattern;

public class slayertimer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Pattern fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$");
    private static final Pattern questStart = Pattern.compile("^ {2}SLAYER QUEST STARTED!$");
    
    public static int BossId = -1;
    public static boolean isFighting = false;
    private static long starttime = 0, spawntime = 0;
    private static int serverticks = 0;

    public static void initialize() {
        TickScheduler.register();
        featManager.register(new slayertimer(), () -> {
            EventBus.register(EventTypes.ClientTickEvent.class, slayertimer.class, e -> {
                if (isFighting) serverticks++;
            });
            EventBus.register(EventTypes.GameMessageEvent.class, slayertimer.class, slayertimer::handleGameMessage);
            EventBus.register(EventTypes.EntityLoadEvent.class, slayertimer.class, e -> {
                if (!isFighting) onEntitySpawn(e.entity, e.entity.getId());
            });
            EventBus.register(EventTypes.EntityUnloadEvent.class, slayertimer.class, e -> {
                if (isFighting) onEntityDeath(e.entity, e.entity.getId());
            });
        });
    }

    private static void handleGameMessage(EventTypes.GameMessageEvent event) {
        if (event.overlay) return;
        String text = event.message.getString();
        if (fail.matcher(text).matches()) onSlayerFailed();
        if (questStart.matcher(text).matches()) spawntime = System.currentTimeMillis();
    }

    private static void onEntitySpawn(Entity entity, int entityId) {
        if (!(entity instanceof net.minecraft.entity.decoration.ArmorStandEntity)) return;
        TickScheduler.schedule(2, () -> {
            String name = entity.getName().getString();
            if (!name.contains("Spawned by")) return;
            String[] parts = name.split("by: ");
            if (parts.length > 1 && Objects.requireNonNull(mc.player).getName().getString().equals(parts[1])) {
                BossId = entityId - 3;
                starttime = System.currentTimeMillis();
                isFighting = true;
                serverticks = 0;
                resetSpawnTimer();
            }
        });
    }

    private static void onEntityDeath(Entity entity, int entityId) {
        if (!(entity instanceof LivingEntity) || entityId != BossId) return;
        long timetaken = System.currentTimeMillis() - starttime;
        sendTimerMessage("killed your boss", timetaken, serverticks);
        resetBossTracker();
    }

    private static void onSlayerFailed() {
        if (!isFighting) return;
        long timetaken = System.currentTimeMillis() - starttime;
        sendTimerMessage("boss killed you", timetaken, serverticks);
        resetBossTracker();
    }

    private static void sendTimerMessage(String action, long timetaken, int ticks) {
        double seconds = timetaken / 1000.0;
        double servertime = ticks / 20.0;
        String content = String.format("§c[Zen] §fYour %s in §b%.2fs §7| §b%.2fs", action, seconds, servertime);
        String hovercontent = String.format("§c%d ms §f| §c%.0f ticks", timetaken, (float)ticks);
        MutableText message = Text.literal(content).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Text.literal(hovercontent))));
        Objects.requireNonNull(mc.player).sendMessage(message, false);
    }

    private static void resetBossTracker() {
        BossId = -1;
        starttime = 0;
        isFighting = false;
        serverticks = 0;
    }

    private static void resetSpawnTimer() {
        if (spawntime == 0L) return;
        double spawnsecond = (System.currentTimeMillis() - spawntime) / 1000.0;
        String content = String.format("§c[Zen] §fYour boss spawned in §b%.2fs", spawnsecond);
        chatutils.clientmsg(content);
        spawntime = 0;
    }
}
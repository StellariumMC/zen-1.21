package meowing.zen.feats.slayers;

import meowing.zen.utils.chatutils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import meowing.zen.Zen;
import meowing.zen.utils.TickScheduler;

import java.util.Objects;
import java.util.regex.Pattern;

public class slayertimer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Pattern fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$");
    private static final Pattern questStart = Pattern.compile("^ {2}SLAYER QUEST STARTED!$");

    public static int BossId = -1;
    public static boolean isFighting = false;
    private static long starttime = 0;
    private static int serverticks = 0;
    private static long spawntime = 0;

    public static void initialize() {
        TickScheduler.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isFighting && Zen.getConfig().slayerkilltimer) serverticks++;
        });
        ClientReceiveMessageEvents.GAME.register(((message, overlay) -> {
            if (!Zen.getConfig().slayerkilltimer || overlay) return;
            String text = message.getString();
            if (fail.matcher(text).matches()) onSlayerFailed();
            if (questStart.matcher(text).matches()) spawntime = System.currentTimeMillis();
        }));
        ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
            if (!Zen.getConfig().slayerkilltimer || isFighting) return;
            onEntitySpawn(entity, entity.getId());
        });
        ClientEntityEvents.ENTITY_UNLOAD.register(((entity, clientWorld) -> {
            if (!Zen.getConfig().slayerkilltimer || !isFighting) return;
            onEntityDeath(entity, entity.getId());
        }));
    }

    private static void onEntitySpawn(Entity entity, int entityId) {
        if (!(entity instanceof net.minecraft.entity.decoration.ArmorStandEntity)) return;
        TickScheduler.schedule(2, () -> {
            String name = entity.getName().getString();
            if (!(name.contains("Spawned by"))) return;
            String[] parts = name.split("by: ");
            if (parts.length > 1) {
                String playername = parts[1];
                ClientPlayerEntity Player = mc.player;
                if (!(Objects.requireNonNull(Player).getName().getString().equals(playername)) || isFighting) return;
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
        double seconds = (double) timetaken / 1000;
        double servertime = (double) serverticks / 20;
        String content = String.format("§c[Zen] §fYou killed your boss in §b%.2fs §7| §b%.2fs", seconds, servertime);
        String hovercontent = String.format("§c%d ticks §f| §c%d ms", serverticks, timetaken);
        MutableText message = Text.literal(content).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Text.literal(hovercontent))));
        Objects.requireNonNull(mc.player).sendMessage(message, false);

        resetBossTracker();
    }

    private static void onSlayerFailed() {
        if (!isFighting) return;
        long timetaken = System.currentTimeMillis() - starttime;
        double seconds = (double) timetaken / 1000;
        double servertime = (double) serverticks / 1000;
        String content = String.format("§c[Zen] §fYour boss killed you in §b%.2fs §7| §b%.2fs", seconds, servertime);
        String hovercontent = String.format("§c%d ticks §f| §c%d ms", serverticks, timetaken);
        MutableText message = Text.literal(content).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Text.literal(hovercontent))));
        Objects.requireNonNull(mc.player).sendMessage(message, false);

        resetBossTracker();
    }

    private static void resetBossTracker() {
        BossId = -1;
        starttime = 0;
        isFighting = false;
        serverticks = 0;
    }

    private static void resetSpawnTimer() {
        double spawnsecond = (spawntime != 0L) ? (System.currentTimeMillis() - spawntime) / 1000.0 : 0.0;
        if ((spawntime == 0L)) return;
        String content = String.format("§c[Zen] §fYour boss spawned in §b%.2fs", spawnsecond);
        chatutils.clientmsg(content);
        spawntime = 0;
    }
}
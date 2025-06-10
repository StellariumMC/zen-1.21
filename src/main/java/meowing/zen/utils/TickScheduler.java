package meowing.zen.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class TickScheduler {
    private static final TickScheduler INSTANCE = new TickScheduler();
    private final Queue<ScheduledTask> taskQueue = new ConcurrentLinkedQueue<>();
    private record ScheduledTask(long executeTick, Runnable action) {}
    private TickScheduler() {}

    public static void schedule(long delayTicks, Runnable action) {
        final long executionTime = System.currentTimeMillis() + (delayTicks * 50);
        INSTANCE.taskQueue.add(new ScheduledTask(executionTime, action));
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        final long currentTime = System.currentTimeMillis();
        while (!taskQueue.isEmpty()) {
            ScheduledTask task = taskQueue.peek();
            if (task == null) break;

            if (currentTime >= task.executeTick()) {
                task.action().run();
                taskQueue.poll();
            } else {
                break;
            }
        }
    }
}
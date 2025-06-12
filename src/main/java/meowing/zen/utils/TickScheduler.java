package meowing.zen.utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import java.util.PriorityQueue;
import java.util.Comparator;

public final class TickScheduler {
    private static final TickScheduler instance = new TickScheduler();
    private final PriorityQueue<ScheduledTask> taskQueue = new PriorityQueue<>(Comparator.comparingLong(ScheduledTask::executeTick));
    private long currentTick = 0;

    private record ScheduledTask(long executeTick, Runnable action) {}

    private TickScheduler() {}

    public static void schedule(long delayTicks, Runnable action) {
        instance.taskQueue.offer(new ScheduledTask(instance.currentTick + delayTicks, action));
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(instance::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        currentTick++;
        ScheduledTask task;
        while ((task = taskQueue.peek()) != null && currentTick >= task.executeTick()) taskQueue.poll().action().run();
    }
}
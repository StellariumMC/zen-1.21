package meowing.zen.utils

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import java.util.PriorityQueue
import java.util.Comparator

object TickScheduler {
    private val taskQueue = PriorityQueue<ScheduledTask>(Comparator.comparingLong(ScheduledTask::executeTick))
    private var currentTick: Long = 0

    private data class ScheduledTask(val executeTick: Long, val action: Runnable)

    fun schedule(delayTicks: Long, action: Runnable) {
        taskQueue.offer(ScheduledTask(currentTick + delayTicks, action))
    }

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register(::onClientTick)
    }

    private fun onClientTick(client: MinecraftClient) {
        currentTick++
        var task: ScheduledTask?
        while (taskQueue.peek().also { task = it } != null && currentTick >= task!!.executeTick) {
            taskQueue.poll().action.run()
        }
    }
}
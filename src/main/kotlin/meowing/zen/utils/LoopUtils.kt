package meowing.zen.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object LoopUtils {
    private val executor = Executors.newScheduledThreadPool(2)
    private val tasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    fun setTimeout(delay: Long, callback: Runnable) = executor.schedule(callback, delay, TimeUnit.MILLISECONDS)

    fun loop(delay: Long, stop: () -> Boolean = { false }, func: () -> Unit): String {
        val id = "${System.nanoTime()}"
        scheduleLoop(id, { delay }, stop, func)
        return id
    }

    fun loop(delay: () -> Number, stop: () -> Boolean = { false }, func: () -> Unit): String {
        val id = "${System.nanoTime()}"
        scheduleLoop(id, { delay().toLong() }, stop, func)
        return id
    }

    fun removeLoop(id: String) = tasks.remove(id)?.cancel(false) ?: false

    private fun scheduleLoop(id: String, delay: () -> Long, stop: () -> Boolean, func: () -> Unit) {
        val task = Runnable {
            try {
                func()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!stop()) {
                tasks[id] = executor.schedule({
                    scheduleLoop(id, delay, stop, func)
                }, delay(), TimeUnit.MILLISECONDS)
            } else {
                tasks.remove(id)
            }
        }
        tasks[id] = executor.schedule(task, 0, TimeUnit.MILLISECONDS)
    }
}
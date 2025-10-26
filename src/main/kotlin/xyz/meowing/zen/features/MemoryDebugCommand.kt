package xyz.meowing.zen.features

import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.Zen
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.LoopUtils
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.knit.api.KnitChat
import java.lang.management.ManagementFactory
import java.text.DecimalFormat

@Zen.Command
object MemoryDebugCommand : Commodore("zenmemorydebug", "zmd") {
    private val df = DecimalFormat("#,###.##")

    init {
        literal("gc") {
            runs {
                val before = Runtime.getRuntime().freeMemory()
                System.gc()
                Thread.sleep(100)
                val after = Runtime.getRuntime().freeMemory()
                val freed = (after - before) / (1024.0 * 1024.0)

                msg("§aForced garbage collection!")
                msg("§7Freed: §c${df.format(freed)} MB")
            }
        }

        runs { runDiagnostics() }
    }

    private fun runDiagnostics() {
        val runtime = Runtime.getRuntime()
        val mb = 1024.0 * 1024.0

        msg("§6§l=== Zen Memory Leak Diagnostics ===")

        val totalMemory = runtime.totalMemory() / mb
        val freeMemory = runtime.freeMemory() / mb
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory() / mb

        msg("")
        msg("§e§lJVM Memory:")
        msg("§7Used: §c${df.format(usedMemory)} MB §7/ §a${df.format(maxMemory)} MB")
        msg("§7Free: §a${df.format(freeMemory)} MB")
        msg("§7Percentage: §c${df.format((usedMemory / maxMemory) * 100)}%")

        checkDataUtils()
        checkTickUtils()
        checkLoopUtils()
        checkEventBus()
        checkFeatures()
        checkThreads()
        checkGC()

        msg("")
        msg("§6§l=== Recommendations ===")

        if (usedMemory / maxMemory > 0.85) {
            msg("§c⚠ CRITICAL: Memory usage over 85%!")
            msg("§7- Increase heap size or fix leaks immediately")
        }

        msg("§7- Run §e/zmd gc §7to force garbage collection")
        msg("§7- Monitor values over time to identify growing collections")

        Zen.LOGGER.info("=== Memory Diagnostics ===")
        Zen.LOGGER.info("JVM Memory: ${df.format(usedMemory)}MB / ${df.format(maxMemory)}MB")
    }

    private fun checkDataUtils() {
        msg("")
        msg("§e§lDataUtils (Static Leak Risk):")
        runCatching {
            val field = DataUtils::class.java.getDeclaredField("autosaveIntervals")
            field.isAccessible = true
            val map = field.get(null) as java.util.concurrent.ConcurrentHashMap<*, *>
            val count = map.size
            val size = count * 256L
            msg("§7Autosave Instances: §c$count §7(~${formatBytes(size)})")
            if (count > 50) msg("§c⚠ WARNING: High DataUtils instance count!")
        }.onFailure { msg("§cError reading DataUtils: ${it.message}") }
    }

    private fun checkTickUtils() {
        msg("")
        msg("§e§lTickUtils Task Queues:")
        runCatching {
            val cls = TickUtils::class.java

            val clientQueue = getField<java.util.PriorityQueue<*>>(cls, "clientTaskQueue")
            val serverQueue = getField<java.util.PriorityQueue<*>>(cls, "serverTaskQueue")
            val activeLoops = getField<MutableSet<*>>(cls, "activeLoops")
            val activeDynamic = getField<MutableMap<*, *>>(cls, "activeDynamicLoops")
            val activeTimers = getField<MutableMap<*, *>>(cls, "activeTimers")

            val clientSize = clientQueue.size
            val serverSize = serverQueue.size
            val loopsSize = activeLoops.size
            val dynamicSize = activeDynamic.size
            val timersSize = activeTimers.size

            msg("§7Client Queue: §c$clientSize §7tasks (~${formatBytes(clientSize * 128L)})")
            msg("§7Server Queue: §c$serverSize §7tasks (~${formatBytes(serverSize * 128L)})")
            msg("§7Active Loops: §c$loopsSize §7(~${formatBytes(loopsSize * 64L)})")
            msg("§7Dynamic Loops: §c$dynamicSize §7(~${formatBytes(dynamicSize * 256L)})")
            msg("§7Active Timers: §c$timersSize §7(~${formatBytes(timersSize * 128L)})")

            val total = clientSize + serverSize + loopsSize + dynamicSize + timersSize
            if (total > 1000) msg("§c⚠ WARNING: High task count! Possible leak.")
        }.onFailure { msg("§cError reading TickUtils: ${it.message}") }
    }

    private fun checkLoopUtils() {
        msg("")
        msg("§e§lLoopUtils Tasks:")
        runCatching {
            val tasks = getField<java.util.concurrent.ConcurrentHashMap<*, *>>(LoopUtils::class.java, "tasks")
            val size = tasks.size
            msg("§7Active Tasks: §c$size §7(~${formatBytes(size * 192L)})")
            if (size > 100) msg("§c⚠ WARNING: High LoopUtils task count!")
        }.onFailure { msg("§cError reading LoopUtils: ${it.message}") }
    }

    private fun checkEventBus() {
        msg("")
        msg("§e§lEventBus Listeners:")
        runCatching {
            val listeners = getField<java.util.concurrent.ConcurrentHashMap<*, *>>(EventBus::class.java, "listeners")
            val eventTypes = listeners.size
            val totalCallbacks = listeners.values.sumOf { (it as MutableSet<*>).size }

            msg("§7Event Types: §c$eventTypes")
            msg("§7Total Callbacks: §c$totalCallbacks §7(~${formatBytes(totalCallbacks * 256L)})")
            if (totalCallbacks > 500) msg("§c⚠ WARNING: High callback count! Check for re-registration.")
        }.onFailure { msg("§cError reading EventBus: ${it.message}") }
    }

    private fun checkFeatures() {
        msg("")
        msg("§e§lZen Features:")
        runCatching {
            val features = getField<MutableList<*>>(Zen::class.java, "features")
            val pending = getField<MutableList<*>>(Zen::class.java, "pendingFeatures")

            msg("§7Loaded Features: §c${features.size} §7(~${formatBytes(features.size * 512L)})")
            msg("§7Pending Features: §c${pending.size}")
            if (pending.size > 0) msg("§c⚠ WARNING: Features still pending initialization!")
        }.onFailure { msg("§cError reading Zen Features: ${it.message}") }
    }

    private fun checkThreads() {
        msg("")
        msg("§e§lThread Information:")
        val bean = ManagementFactory.getThreadMXBean()
        msg("§7Active Threads: §c${bean.threadCount} §7(Daemon: §e${bean.daemonThreadCount}§7)")
        msg("§7Peak Threads: §c${bean.peakThreadCount}")
        if (bean.threadCount > 100) msg("§c⚠ WARNING: High thread count! Possible thread leak.")
    }

    private fun checkGC() {
        msg("")
        msg("§e§lGarbage Collection:")
        ManagementFactory.getGarbageCollectorMXBeans().forEach { gc ->
            msg("§7${gc.name}: §c${gc.collectionCount} §7collections, §c${gc.collectionTime}ms")
        }
    }

    private inline fun <reified T> getField(cls: Class<*>, name: String): T {
        val field = cls.getDeclaredField(name)
        field.isAccessible = true
        return field.get(null) as T
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${df.format(bytes / 1024.0)} KB"
        else -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
    }

    private fun msg(text: String) = KnitChat.fakeMessage(text)
}

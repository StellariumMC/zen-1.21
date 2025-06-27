package meowing.zen.feats.slayers

import meowing.zen.events.ChatReceiveEvent
import meowing.zen.events.EntityLeaveEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.feats.Feature
import net.minecraft.entity.LivingEntity

object slayertimer : Feature("slayertimer") {
    @JvmField var BossId = -1
    @JvmField var isFighting = false

    private val fail = Regex("^ {2}SLAYER QUEST FAILED!$")
    private val questStart = Regex("^ {2}SLAYER QUEST STARTED!$")
    private var startTime = 0L
    private var spawnTime = 0L
    private var serverTicks = 0

    override fun initialize() {
        register<ChatReceiveEvent> { event ->
            val text = event.message!!.string.removeFormatting()
            when {
                fail.matches(text) -> onSlayerFailed()
                questStart.matches(text) -> spawnTime = System.currentTimeMillis()
            }
        }

        register<EntityLeaveEvent> { event ->
            if (event.entity is LivingEntity && event.entity.id == BossId && isFighting) {
                val timeTaken = System.currentTimeMillis() - startTime
                slayerstats.addKill(timeTaken)
                sendTimerMessage("You killed your boss", timeTaken)
                resetBossTracker()
            }
        }
    }

    fun handleBossSpawn(entityId: Int) {
        if (isFighting) return
        BossId = entityId - 3
        startTime = System.currentTimeMillis()
        isFighting = true
        serverTicks = 0
        resetSpawnTimer()
        slayerhighlight.update()
    }

    private fun onSlayerFailed() {
        if (!isFighting) return
        val timeTaken = System.currentTimeMillis() - startTime
        sendTimerMessage("Your boss killed you", timeTaken)
        resetBossTracker()
    }

    private fun sendTimerMessage(action: String, timeTaken: Long) {
        val seconds = timeTaken / 1000.0
        val content = "§c[Zen] §f$action in §b${"%.2f".format(seconds)}s"
        val hoverText = "§c${timeTaken}ms"
        ChatUtils.addMessage(content, hoverText)
    }

    private fun resetBossTracker() {
        BossId = -1
        startTime = 0
        isFighting = false
        serverTicks = 0
        slayerhighlight.update()
    }

    private fun resetSpawnTimer() {
        if (spawnTime == 0L) return
        val spawnSeconds = (System.currentTimeMillis() - spawnTime) / 1000.0
        val content = "§c[Zen] §fYour boss spawned in §b${"%.2f".format(spawnSeconds)}s"
        ChatUtils.addMessage(content)
        spawnTime = 0
    }
}
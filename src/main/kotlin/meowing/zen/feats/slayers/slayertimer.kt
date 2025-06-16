package meowing.zen.feats.slayers

import meowing.zen.utils.EventBus
import meowing.zen.utils.EventTypes
import meowing.zen.utils.chatutils
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.text.*
import meowing.zen.featManager
import java.util.regex.Pattern

object slayertimer {
    private val mc = MinecraftClient.getInstance()
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private val questStart = Pattern.compile("^ {2}SLAYER QUEST STARTED!$")

    @JvmField
    var BossId = -1

    @JvmField
    var isFighting = false

    private var starttime = 0L
    private var spawntime = 0L
    private var serverticks = 0

    @JvmStatic
    fun initialize() {
        featManager.register(this) {
            EventBus.register(EventTypes.ClientTickEvent::class.java, this) {
                if (isFighting) serverticks++
            }
            EventBus.register(EventTypes.GameMessageEvent::class.java, this, this::onGameMessage)
            EventBus.register(EventTypes.EntityTrackerUpdateEvent::class.java, this, this::onEntityTrackerUpdate)
            EventBus.register(EventTypes.EntityUnloadEvent::class.java, this, this::onEntityDeath)
        }
    }

    private fun onSlayerFailed() {
        if (!isFighting) return
        val timetaken = System.currentTimeMillis() - starttime
        sendTimerMessage("boss killed you", timetaken, serverticks)
        resetBossTracker()
    }

    private fun sendTimerMessage(action: String, timetaken: Long, ticks: Int) {
        val seconds = timetaken / 1000.0
        val servertime = ticks / 20.0
        val content = "§c[Zen] §fYour $action in §b%.2fs §7| §b%.2fs".format(seconds, servertime)
        val hovercontent = "§c%d ms §f| §c%.0f ticks".format(timetaken, ticks.toFloat())
        val message = Text.literal(content).setStyle(
            Style.EMPTY.withHoverEvent(HoverEvent.ShowText(Text.literal(hovercontent)))
        )
        mc.player?.sendMessage(message, false)
    }

    private fun resetBossTracker() {
        BossId = -1
        starttime = 0
        isFighting = false
        serverticks = 0
    }

    private fun resetSpawnTimer() {
        if (spawntime == 0L) return
        val spawnsecond = (System.currentTimeMillis() - spawntime) / 1000.0
        val content = "§c[Zen] §fYour boss spawned in §b%.2fs".format(spawnsecond)
        chatutils.clientmsg(content, false)
        spawntime = 0
    }

    private fun onGameMessage(event: EventTypes.GameMessageEvent) {
        if (event.overlay) return
        val text = event.getPlainText()
        if (fail.matcher(text).matches()) onSlayerFailed()
        if (questStart.matcher(text).matches()) spawntime = System.currentTimeMillis()
    }

    private fun onEntityTrackerUpdate(event: EventTypes.EntityTrackerUpdateEvent) {
        val customName = event.getCustomName() ?: return
        if (!customName.contains("Spawned by") || isFighting) return

        val parts = customName.split("by: ")
        if (parts.size > 1 && mc.player?.name?.string == parts[1]) {
            BossId = event.getEntityId() - 3
            starttime = System.currentTimeMillis()
            isFighting = true
            serverticks = 0
            resetSpawnTimer()
        }
    }

    private fun onEntityDeath(event: EventTypes.EntityUnloadEvent) {
        if (event.entity !is LivingEntity || event.getEntityId() != BossId) return
        val timetaken = System.currentTimeMillis() - starttime
        sendTimerMessage("killed your boss", timetaken, serverticks)
        resetBossTracker()
    }
}
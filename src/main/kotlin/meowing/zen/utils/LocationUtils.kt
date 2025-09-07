package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.AreaEvent
import meowing.zen.events.EventBus
import meowing.zen.events.GameEvent
import meowing.zen.events.PacketEvent
import meowing.zen.utils.Utils.removeEmotes
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.TeamS2CPacket
import net.minecraft.scoreboard.ScoreboardDisplaySlot

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
object LocationUtils {
    private val areaRegex = "^(?:Area|Dungeon): ([\\w ]+)$".toRegex()
    private val subAreaRegex = "^ ([⏣ф]) .*".toRegex()
    private val uselessRegex = "^[⏣ф] ".toRegex()
    private val lock = Any()
    private var cachedAreas = mutableMapOf<String?, Boolean>()
    private var cachedSubareas = mutableMapOf<String?, Boolean>()
    var dungeonFloor: String? = null
        private set
    var dungeonFloorNum: Int? = null
        private set
    var area: String? = null
        private set
    var subarea: String? = null
        private set
    var inSkyblock = false
        private set

    init {
        EventBus.register<PacketEvent.Received> { event ->
            when (val packet = event.packet) {
                is PlayerListS2CPacket -> {
                    val action = packet.actions.firstOrNull()
                    if (action != PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME && action != PlayerListS2CPacket.Action.ADD_PLAYER) return@register
                    packet.entries?.forEach { entry ->
                        val displayName = entry.displayName?.string ?: return@forEach

                        val line = displayName.removeEmotes()
                        val match = areaRegex.find(line) ?: return@forEach
                        val newArea = match.groupValues[1]
                        if (newArea.lowercase() != area) {
                            synchronized(lock) {
                                EventBus.post(AreaEvent.Main(newArea))
                                area = newArea.lowercase()
                            }
                        }
                    }
                }
                is TeamS2CPacket -> {
                    val prefix = packet.team.orElse(null)?.prefix?.string ?: ""
                    val suffix = packet.team.orElse(null)?.suffix?.string ?: ""
                    if (prefix.isEmpty() || suffix.isEmpty()) return@register

                    val line = prefix + suffix
                    if (!subAreaRegex.matches(line)) return@register
                    if (line.endsWith("cth") || line.endsWith("ch")) return@register
                    val cleanSubarea = line.removeFormatting().replace(uselessRegex, "").trim().lowercase()
                    if (cleanSubarea != subarea) {
                        synchronized(lock) {
                            EventBus.post(AreaEvent.Sub(cleanSubarea))
                            subarea = cleanSubarea
                        }
                    }
                    if (line.contains("The Catacombs (") && !line.contains("Queue")) {
                        dungeonFloor = line.removeFormatting().substringAfter("(").substringBefore(")")
                        dungeonFloorNum = dungeonFloor?.lastOrNull()?.digitToIntOrNull() ?: 0
                    }
                }
            }
        }

        EventBus.register<AreaEvent.Main> {
            synchronized(lock) {
                cachedAreas.clear()
            }
        }

        EventBus.register<AreaEvent.Sub> {
            synchronized(lock) {
                cachedSubareas.clear()
            }
        }

        EventBus.register<GameEvent.Disconnect> {
            reset()
        }

        TickUtils.loop(20) {
            if (mc.world != null) {
                val old = inSkyblock
                inSkyblock = mc.world?.scoreboard?.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)?.name == "SBScoreboard"
                if (old != inSkyblock) EventBus.post(AreaEvent.Skyblock(inSkyblock))
            }
        }
    }

    private fun reset() {
        inSkyblock = false
        dungeonFloor = null
        dungeonFloorNum = null
        subarea = null
        area = null
    }

    fun checkArea(areaLower: String?): Boolean {
        return synchronized(lock) {
            cachedAreas.getOrPut(areaLower) {
                areaLower?.let { area == it } ?: true
            }
        }
    }

    fun checkSubarea(subareaLower: String?): Boolean {
        return synchronized(lock) {
            cachedSubareas.getOrPut(subareaLower) {
                subareaLower?.let { subarea?.contains(it) == true } ?: true
            }
        }
    }
}
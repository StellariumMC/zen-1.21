package meowing.zen.utils

import meowing.zen.events.AreaEvent
import meowing.zen.events.EventBus
import meowing.zen.events.PacketEvent
import meowing.zen.utils.Utils.removeEmotes
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket
import net.minecraft.network.packet.s2c.play.TeamS2CPacket

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
    var area: String? = null
        private set
    var subarea: String? = null
        private set

    init {
        EventBus.register<PacketEvent.Received> ({ event ->
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
                }
            }
        })

        EventBus.register<AreaEvent.Main> ({
            synchronized(lock) {
                cachedAreas.clear()
            }
        })

        EventBus.register<AreaEvent.Sub> ({
            synchronized(lock) {
                cachedSubareas.clear()
            }
        })
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
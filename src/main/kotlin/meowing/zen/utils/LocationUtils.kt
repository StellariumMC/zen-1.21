package meowing.zen.utils

import meowing.zen.events.*
import meowing.zen.utils.Utils.removeEmotes
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.TeamS2CPacket

object LocationUtils {
    private val areaRegex = "^(?:Area|Dungeon): ([\\w ]+)$".toRegex()
    private val subAreaRegex = "^ ([⏣ф]) .*".toRegex()
    var area: String? = null
    var subarea: String? = null

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
                            EventBus.post(AreaEvent(newArea))
                            area = newArea.lowercase()
                        }
                    }
                }
                is TeamS2CPacket -> {
                    val teamData = packet.team.orElse(null) ?: return@register
                    val prefix = teamData.prefix?.string ?: ""
                    val suffix = teamData.suffix?.string ?: ""
                    if (prefix.isEmpty() || suffix.isEmpty()) return@register

                    val line = prefix + suffix
                    if (!subAreaRegex.matches(line)) return@register
                    if (line.lowercase() != subarea) {
                        EventBus.post(SubAreaEvent(line))
                        subarea = line.lowercase()
                    }
                }
            }
        })
    }
}
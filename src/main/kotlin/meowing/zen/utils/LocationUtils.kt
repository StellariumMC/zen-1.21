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
                    when (packet.actions.firstOrNull()) {
                        PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,
                        PlayerListS2CPacket.Action.ADD_PLAYER -> {
                            packet.entries?.forEach { entry ->
                                val displayName = entry.displayName?.string ?: return@forEach
                                val line = displayName.removeEmotes()
                                if (areaRegex.matches(line)) {
                                    val newArea = areaRegex.find(line)?.groupValues?.get(1) ?: return@forEach
                                    if (newArea != area) {
                                        EventBus.post(AreaEvent(newArea))
                                        area = newArea.lowercase()
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
                is TeamS2CPacket -> {
                    val teamData = packet.team.get()
                    val prefix = teamData.prefix?.string ?: ""
                    val suffix = teamData.suffix?.string ?: ""
                    if (prefix.isEmpty() || suffix.isEmpty()) return@register
                    val line = "$prefix$suffix"
                    if (subAreaRegex.matches(line) && line != subarea) {
                        EventBus.post(SubAreaEvent(line))
                        subarea = line.lowercase()
                    }
                }
            }
        })
    }
}
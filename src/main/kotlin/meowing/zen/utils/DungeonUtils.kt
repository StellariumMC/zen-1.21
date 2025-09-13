package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.api.DungeonsAPI
import meowing.zen.events.EventBus
import meowing.zen.events.AreaEvent
import meowing.zen.events.ChatEvent
import meowing.zen.events.TablistEvent
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import meowing.zen.utils.Utils.removeFormatting
import kotlin.collections.mutableMapOf
import kotlin.math.floor

object DungeonUtils {
    private val cryptsRegex = "^ Crypts: (\\d+)$".toRegex()
    private val cataRegex = "^ Catacombs (\\d+):".toRegex()
    private val playerInfoRegex = "^[^\\x00-\\x7F]?(?:\\[\\d+] )?(?:\\[\\w+] )?(\\w{1,16})(?: [^\\x00-\\x7F]+)? \\((\\w+) ?(([IVXLCDM]+))?\\)$".toRegex()
    private val completeRegex = """^\s*(Master Mode)?\s?(?:The)? Catacombs - (Entrance|Floor .{1,3})$""".toRegex()
    private var crypts = 0
    private var currentClass: String? = null
    private var currentLevel = 0
    private val players = mutableMapOf<String, PlayerData>()
    private var cryptsTab: EventBus.EventCall? = null

    data class PlayerData(
        val name: String,
        val className: String,
        val level: Int,
        var secrets: Int = 0,
        var intSecrets: Int = 0,
        var uuid: String  = ""
    )

    data class PersistentData(var cataLevel: Int = 0)
    private val Data = DataUtils("DungeonUtils", PersistentData())

    init {
        EventBus.register<AreaEvent.Main> ({ event ->
            val inCatacombs = event.area.equals("catacombs", true)

            if (inCatacombs && cryptsTab == null) {
                cryptsTab = EventBus.register<TablistEvent.Update> ({ tabEvent ->
                    tabEvent.packet.entries.forEach { entry ->
                        val text = entry.displayName?.string?.removeFormatting() ?: return@forEach

                        cryptsRegex.find(text)?.let {
                            crypts = it.groupValues[1].toIntOrNull() ?: crypts
                        }

                        playerInfoRegex.find(text)?.let { match ->
                            val playerName = match.groupValues[1]
                            val className = match.groupValues[2]
                            val levelStr = match.groupValues[4]
                            val level = if (levelStr.isNotEmpty()) Utils.decodeRoman(levelStr) else 0

                            players[playerName] = PlayerData(playerName, className, level)

                            if (playerName == mc.player?.name?.string) {
                                currentClass = className
                                currentLevel = level
                            }
                        }
                    }
                })
            }

            if (!inCatacombs) {
                cryptsTab?.unregister()
                cryptsTab = null
                reset()
            }
        })

        EventBus.register<TablistEvent.Update> ({ event ->
            event.packet.entries.forEach { entry ->
                val text = entry.displayName?.string?.removeFormatting() ?: return@forEach
                cataRegex.find(text)?.let { match ->
                    val cata = match.groupValues[1].toIntOrNull()
                    if (cata != null) updateData { it.cataLevel = cata }
                }
            }
        })

        EventBus.register<WorldEvent.Change> ({
            cryptsTab?.unregister()
            cryptsTab = null
            reset()
        })

        EventBus.register<TickEvent.Client> {
            players.forEach { (player, info) ->
                println("Checking player $players for a player entity")
                val playerObj = mc.world?.players?.firstOrNull { it.name.string == player } ?: return@forEach
                println("Has a player")

                val uuid = playerObj.uuid.toString()
                info.uuid = uuid

                DungeonsAPI.fetchSecrets(uuid, cacheMs = 120_000) { secrets ->
                    info.intSecrets = secrets
                    info.secrets = secrets
                }
            }
        }

        EventBus.register<ChatEvent.Receive> { event ->
            completeRegex.find(event.message.string.removeFormatting()) ?: return@register
            players.forEach { (player, info) ->
                DungeonsAPI.fetchSecrets(info.uuid, cacheMs = 0) { secrets ->
                    info.secrets = secrets
                }
            }
        }
    }

    private fun reset() {
        crypts = 0
        currentClass = null
        currentLevel = 0
        players.clear()
    }

    fun getPlayers(): MutableMap<String, PlayerData> = players

    fun getCryptCount(): Int = crypts

    fun getCurrentClass(): String? = currentClass

    fun getCurrentLevel(): Int = currentLevel

    fun isMage(): Boolean = currentClass == "Mage"

    fun getPlayerClass(playerName: String): String? = players[playerName]?.className

    fun isDuplicate(className: String): Boolean = players.values.count { it.className.equals(className, true) } > 1

    fun getMageReduction(cooldown: Double, checkClass: Boolean = false): Double {
        if (checkClass && currentClass != "Mage") return cooldown

        val multiplier = if (isDuplicate("mage")) 1 else 2
        return cooldown * (0.75 - (floor(currentLevel / 2.0) / 100.0) * multiplier)
    }

    private fun updateData(updater: (PersistentData) -> Unit) {
        val currentData = Data.getData()
        updater(currentData)
        Data.setData(currentData)
    }

    // TODO: Use api for cata level and calc
    fun getCurrentCata(): Int = Data.getData().cataLevel
}

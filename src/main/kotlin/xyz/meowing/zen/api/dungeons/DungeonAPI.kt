package xyz.meowing.zen.api.dungeons

import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.parseDuration
import tech.thatgravyboat.skyblockapi.utils.extentions.parseRomanOrArabic
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.find
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findThenNull
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import xyz.meowing.knit.api.KnitPlayer
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.PlayerEvent
import xyz.meowing.zen.events.core.ScoreboardEvent
import xyz.meowing.zen.events.core.TablistEvent
import kotlin.time.Duration

/**
 * Slightly modified version of SkyblockAPI's DungeonAPI to allow us to modify it to fit our needs.
 *
 * Original File: [GitHub](https://github.com/SkyblockAPI/SkyblockAPI/blob/2.0/src/common/main/kotlin/tech/thatgravyboat/skyblockapi/api/area/dungeon/DungeonAPI.kt)
 * @author SkyblockAPI
 */
@Module
object DungeonAPI {
    private val cryptsRegex = Regex("^ Crypts: (?<count>\\d+)$")
    private val dungeonFloorRegex = Regex("The Catacombs \\((?<floor>.+)\\)")
    private val timeRegex = Regex("Time Elapsed: (?<time>[\\dhms ]+)")
    private val roomIdRegex = Regex("\\d+/\\d+/\\d+ .+? (?<id>.+)")
    private val partyAmountRegex = Regex("\\s*Party \\((?<amount>\\d+)\\)")
    private val classRegex = Regex("(?:\\[.+] ?)*(?<name>\\S+) .*\\((?<class>\\S+) (?<level>.+)\\)")
    private val deadTeammateRegex = Regex("\\[.+] (?<name>\\S+) .*\\(DEAD\\)")
    private val milestoneRegex = Regex("\\s*Your Milestone: ☠(?<milestone>.)")
    private val startRegex = Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.")
    private val uniqueClassRegex = Regex("Your .+ stats are doubled because you are the only player using this class!")
    private val bossStartRegex = Regex( "^\\[BOSS] (?<boss>.+?):")
    private val endRegex = Regex("\\s+(?:Master Mode|The) Catacombs - (?:Entrance|Floor [XVI]+)")

    var cryptCount: Int = 0
        private set
    var ownPlayer: DungeonPlayer? = null
        private set

    val dungeonClass: DungeonClass? get() = ownPlayer?.dungeonClass

    val classLevel: Int get() = ownPlayer?.classLevel ?: 0

    var dungeonFloor: DungeonFloor? = null
        private set(value) {
            if (field != value) {
                field = value
                EventBus.post(LocationEvent.DungeonFloorChange(value))
            }
        }

    var uniqueClass: Boolean = false
        private set

    var started: Boolean = false
        private set

    var completed: Boolean = false
        private set

    var inBoss: Boolean = false
        private set

    var milestone: Int = 0
        private set

    var partySize: Int = 0
        private set

    var teammates: List<DungeonPlayer> = emptyList()
        private set

    var time: Duration = Duration.ZERO
        private set

    var roomId: String? = null
        private set

    init {
        EventBus.register<LocationEvent.IslandChange> { reset() }

        EventBus.registerIn<TablistEvent.Change>(SkyBlockIsland.THE_CATACOMBS) { event ->
            val firstColumn = event.new.firstOrNull() ?: return@registerIn
            val first = firstColumn.firstOrNull() ?: return@registerIn

            partyAmountRegex.find(first.stripped, "amount") { (amount) ->
                this.partySize = amount.toIntValue()
            }

            val ownName = KnitPlayer.name

            for (line in firstColumn) {
                val stripped = line.stripped

                classRegex.findThenNull(stripped, "name", "class", "level") { (name, dungeonClass, level) ->
                    val dungeonPlayer = teammates.find { it.name == name }

                    if (dungeonPlayer != null) {
                        if (name != ownName) dungeonPlayer.dead = false

                        if (dungeonPlayer.missingData()) {
                            dungeonPlayer.dungeonClass = DungeonClass.Companion.getByName(dungeonClass)
                            dungeonPlayer.classLevel = level.parseRomanOrArabic()
                        }

                        return@findThenNull
                    }

                    val playerClass = DungeonClass.Companion.getByName(dungeonClass)
                    val player = DungeonPlayer(name, playerClass, level.parseRomanOrArabic())

                    if (name == ownName) ownPlayer = player

                    teammates += player
                } ?: continue

                deadTeammateRegex.find(stripped, "name") { (name) ->
                    var dungeonPlayer = teammates.find { it.name == name }

                    if (dungeonPlayer == null) {
                        dungeonPlayer = DungeonPlayer(name, null, null)
                        teammates += dungeonPlayer
                    }

                    dungeonPlayer.dead = true
                }
            }

            val secondColumn = event.new.getOrNull(1) ?: return@registerIn

            for (line in secondColumn) {
                val stripped = line.stripped

                milestoneRegex.find(stripped, "milestone") { (milestone) ->
                    this.milestone = milestoneCharToInt(milestone.first())
                }

                cryptsRegex.find(stripped, "count") { (count) ->
                    cryptCount = count.toIntOrNull() ?: cryptCount
                }
            }
        }

        EventBus.registerIn<LocationEvent.AreaChange> (SkyBlockIsland.THE_CATACOMBS) { event ->
            dungeonFloorRegex.find(event.new.name, "floor") { (floor) ->
                dungeonFloor = DungeonFloor.Companion.getByName(floor)
            }
        }

        EventBus.registerIn<ScoreboardEvent.Update> (SkyBlockIsland.THE_CATACOMBS) { event ->
            for (line in event.added) {
                timeRegex.findThenNull(line, "time") { (time) ->
                    this.time = time.parseDuration() ?: return@findThenNull
                } ?: continue

                roomIdRegex.findThenNull(line, "id") { (roomId) ->
                    this.roomId = roomId
                } ?: continue
            }
        }

        EventBus.registerIn<ChatEvent.Receive> (SkyBlockIsland.THE_CATACOMBS) { event ->
            if (event.isActionBar) return@registerIn
            val message = event.message.string

            if (!started && startRegex.matches(message)) {
                started = true
                return@registerIn
            }

            if (uniqueClassRegex.matches(message)) {
                uniqueClass = true
                return@registerIn
            }

            if (!inBoss && dungeonFloor != DungeonFloor.E) {
                bossStartRegex.findThenNull(message, "boss") { (boss) ->
                    if (boss != "The Watcher") return@findThenNull
                    inBoss = dungeonFloor?.chatBossName == boss
                } ?: return@registerIn
            }

            if (started && endRegex.matches(message)) {
                completed = true
                return@registerIn
            }
        }

        EventBus.registerIn<PlayerEvent.HotbarChange> (SkyBlockIsland.THE_CATACOMBS) { event ->
            if (event.slot != 0) return@registerIn

            val id = event.item.getData(DataTypes.ID)
            ownPlayer?.dead = id == "HAUNT_ABILITY"
        }
    }

    private fun milestoneCharToInt(char: Char): Int = if (char in '❶'..'❾') '❶'.code - char.code + 1 else 0

    private fun reset() {
        dungeonFloor = null
        ownPlayer = null
        uniqueClass = false
        started = false
        completed = false
        inBoss = false
        milestone = 0
        partySize = 0
        cryptCount = 0
        teammates = emptyList()
        time = Duration.ZERO
        roomId = null
    }
}
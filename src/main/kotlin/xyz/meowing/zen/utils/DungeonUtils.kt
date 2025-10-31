package xyz.meowing.zen.utils

import kotlin.math.floor

object DungeonUtils {
    private val cryptsRegex = "^ Crypts: (\\d+)$".toRegex()
    private val cataRegex = "^ Catacombs (\\d+):".toRegex()
    private val playerInfoRegex = "^[^\\x00-\\x7F]?(?:\\[\\d+] )?(?:\\[\\w+] )?(\\w{1,16})(?: [^\\x00-\\x7F]+)? \\((\\w+) ?(([IVXLCDM]+))?\\)$".toRegex()
    private var crypts = 0
    private var currentClass: String? = null
    private var currentLevel = 0
    private val players = mutableMapOf<String, PlayerData>()

    data class PlayerData(val name: String, val className: String, val level: Int)

    data class PersistentData(var cataLevel: Int = 0)
    private val Data = DataUtils("DungeonUtils", PersistentData())

    init {
    }

    private fun reset() {
        crypts = 0
        currentClass = null
        currentLevel = 0
        players.clear()
    }

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
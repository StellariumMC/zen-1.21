package xyz.meowing.zen.utils

import net.minecraft.client.multiplayer.PlayerInfo
import xyz.meowing.zen.Zen
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.level.GameType
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player

object ScoreboardUtils {
    // Modified from Skyblocker https://github.com/SkyblockerMod/Skyblocker
    fun getSidebarLines(): List<String> {
        return try {
            //#if MC >= 1.21.9
            //$$ val scoreboard = world?.scoreboard ?: return emptyList()
            //#else
            val scoreboard = player?.scoreboard ?: return emptyList()
            //#endif

            val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()

            val stringLines = mutableListOf<String>()
            val scoreHolders = scoreboard.trackedPlayers.toList()

            // Loop over all known scoreboard entries
            for (scoreHolder in scoreHolders) {
                if (!scoreboard.listPlayerScores(scoreHolder).containsKey(objective)) continue
                // Only include entries that are part of the current objective
                val objectivesForEntry = scoreboard.listPlayerScores(scoreHolder)
                if (!objectivesForEntry.containsKey(objective)) continue

                val team = scoreboard.getPlayersTeam(scoreHolder.scoreboardName)

                if (team != null) {
                    val strLine = team.playerPrefix.string + team.playerSuffix.string
                    if (strLine.trim().isNotEmpty()) stringLines.add(strLine)
                }
            }

            // Add the objective title at the end (top of sidebar)
            val objectiveTitle = objective.displayName
            stringLines.add(objectiveTitle.string)

            // Reverse so the sidebar order is correct
            return stringLines.reversed()
        } catch (e: Exception) {
            Zen.LOGGER.warn("Error in getSidebarLines: $e")
            emptyList()
        }
    }


    fun getScoreboardTitle(cleanColor: Boolean = true): String? {
        val scoreboard = world?.scoreboard ?: return null
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null

        return objective.displayName?.string?.let {
            if (cleanColor) it.removeFormatting() else it
        }
    }

    /**
     * This code is modified
     * @Author: nea98
     * @Source: https://moddev.nea.moe
     **/
    private fun stripAlienCharacters(text: String): String {
        return text.filter {
            client.font.width(it.toString()) > 0 || it == '§'
        }
    }

    fun getTabListEntries(): List<PlayerInfo> {
        val networkHandler = client.connection ?: return emptyList()
        return networkHandler.onlinePlayers
                .sortedWith(compareBy<PlayerInfo> { it.gameMode == GameType.SPECTATOR }
                    .thenBy { it.team?.name ?: "" }
                    .thenBy { it.profile.name })

    }

    fun getTabListEntriesString(): List<String> {
        return getTabListEntries()
            .mapNotNull { it.tabListDisplayName?.string }
            .filter { it.isNotEmpty() }
    }
}
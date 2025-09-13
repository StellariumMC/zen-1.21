package meowing.zen.utils

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.scoreboard.ScoreboardDisplaySlot

object ScoreboardUtils {
    // Modified from Skyblocker https://github.com/SkyblockerMod/Skyblocker
    fun getSidebarLines(cleanColor: Boolean): List<String> {
        return try {
            val scoreboard = mc.player?.scoreboard ?: return emptyList()
            val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()

            val stringLines = mutableListOf<String>()

            // Loop over all known scoreboard entries
            for (scoreHolder in scoreboard.knownScoreHolders) {
                if (!scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) continue
                // Only include entries that are part of the current objective
                val objectivesForEntry = scoreboard.getScoreHolderObjectives(scoreHolder)
                if (!objectivesForEntry.containsKey(objective)) continue

                val team = scoreboard.getScoreHolderTeam(scoreHolder.nameForScoreboard)

                if (team != null) {
                    val strLine = team.prefix.string + team.suffix.string

                    if (!strLine.trim().isEmpty()) stringLines.add(strLine)
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
        val scoreboard = mc.world?.scoreboard ?: return null
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return null

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
            mc.textRenderer.getWidth(it.toString()) > 0 || it == '§'
        }
    }

    fun getTabListEntries(): List<String> {
        val playerList = mc.networkHandler?.playerList ?: return emptyList()
        return playerList.map { playerInfo ->
            playerInfo.displayName?.string ?: playerInfo.profile.name
        }
    }
}
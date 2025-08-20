package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.text.Text

object ScoreboardUtils {
    fun getSidebarLines(cleanColor: Boolean): List<String> {
        val scoreboard = mc.world?.scoreboard ?: return emptyList()
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()

        return scoreboard.getScoreboardEntries(objective)
            .mapNotNull { entry ->
                entry.comp_2127()?.let { owner ->
                    stripAlienCharacters(
                        scoreboard.getTeam(owner)?.decorateName(Text.literal(owner))?.string ?: owner
                    ).let {
                        if (cleanColor) it.removeFormatting()
                        else it
                    }
                }
            }
            .reversed()
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
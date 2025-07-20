package meowing.zen.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ScoreboardUtils {
    private val mc = MinecraftClient.getInstance()

    fun getSidebarLines(cleanColor: Boolean): List<String> {
        val scoreboard = mc.world?.scoreboard ?: return emptyList()
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()

        return scoreboard.getScoreboardEntries(objective)
            .mapNotNull { entry ->
                entry.owner()?.let { owner ->
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

fun String.removeFormatting(): String {
    return Formatting.strip(this) ?: this
}
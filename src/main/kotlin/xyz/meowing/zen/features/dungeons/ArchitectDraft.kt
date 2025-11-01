package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.knit.api.text.core.ClickEvent
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object ArchitectDraft : Feature("architectdraft", island = SkyBlockIsland.THE_CATACOMBS) {
    private val puzzlefail = "^PUZZLE FAIL! (\\w{1,16}) .+$".toRegex()
    private val quizfail = "^\\[STATUE] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.$".toRegex()
    private val autogetdraft by ConfigDelegate<Boolean>("autogetdraft")
    private val selfdraft by ConfigDelegate<Boolean>("selfdraft")

    override fun addConfig() {
        ConfigManager
            .addFeature("Architect Draft Message", "", "Dungeons", ConfigElement(
                "architectdraft",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Only get drafts on your fails", "", "Options", ConfigElement(
                "selfdraft",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Automatically get architect drafts", "", "Options", ConfigElement(
                "autogetdraft",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register

            val text = event.message.string.removeFormatting()
            val matchResult = puzzlefail.find(text) ?: quizfail.find(text) ?: return@register
            val playerName = Utils.currentPlayerName


            if (matchResult.groupValues[1] != playerName && selfdraft) return@register

            if (autogetdraft) {
                TickUtils.schedule(40) {
                    KnitChat.sendCommand("gfs architect's first draft 1")
                }
            } else {
                val archMessage = KnitText
                    .literal("$prefix §bClick to get Architect's First Draft from Sack.")
                    .onClick(ClickEvent.RunCommand("/gfs architect's first draft 1"))
                    .toVanilla()
                KnitChat.fakeMessage(archMessage)
            }
        }
    }
}
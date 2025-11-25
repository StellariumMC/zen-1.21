package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.knit.api.text.core.ClickEvent
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object ArchitectDraft : Feature(
    "architectDraft",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private val puzzleFailRegex = "^PUZZLE FAIL! (\\w{1,16}) .+$".toRegex()
    private val quizFailRegex = "^\\[STATUE] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.$".toRegex()
    private val onlySelf by ConfigDelegate<Boolean>("architectDraft.onlySelf")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Architect Draft Message",
                "",
                "Dungeons",
                ConfigElement(
                    "architectDraft",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Only for yourself",
                ConfigElement(
                    "architectDraft.onlySelf",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register

            val text = event.message.string.removeFormatting()
            val matchResult = puzzleFailRegex.find(text) ?: quizFailRegex.find(text) ?: return@register

            if (matchResult.groupValues[1] != player?.name?.string && onlySelf) return@register

            val archMessage = KnitText
                .literal("$prefix §bClick to get Architect's First Draft from Sack.")
                .onClick(ClickEvent.RunCommand("/gfs architect's first draft 1"))

            KnitChat.fakeMessage(archMessage)
        }
    }
}
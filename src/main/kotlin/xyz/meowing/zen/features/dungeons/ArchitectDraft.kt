package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.text.ClickEvent
import xyz.meowing.zen.ui.ConfigManager

@Zen.Module
object ArchitectDraft : Feature("architectdraft", area = "catacombs") {
    private val puzzlefail = "^PUZZLE FAIL! (\\w{1,16}) .+$".toRegex()
    private val quizfail = "^\\[STATUE] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.$".toRegex()
    private val autogetdraft by ConfigDelegate<Boolean>("autogetdraft")
    private val selfdraft by ConfigDelegate<Boolean>("selfdraft")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        ConfigManager
            .addFeature("Architect Draft Message", "", "Dungeons", xyz.meowing.zen.ui.ConfigElement(
                "architectdraft",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Only get drafts on your fails", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "selfdraft",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Automatically get architect drafts", "", "Options", xyz.meowing.zen.ui.ConfigElement(
                "autogetdraft",
                ElementType.Switch(false)
            ))

        return configUI
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()
            val matchResult = puzzlefail.find(text) ?: quizfail.find(text) ?: return@register

            if (matchResult.groupValues[1] != player?.name?.string && selfdraft) return@register

            if (autogetdraft) {
                TickUtils.schedule(40) {
                    ChatUtils.command("/gfs architect's first draft 1")
                }
            } else {
                ChatUtils.addMessage(
                    "$prefix §bClick to get Architect's First Draft from Sack.",
                    clickAction = ClickEvent.Action.RUN_COMMAND,
                    clickValue = "/gfs architect's first draft 1"
                )
            }
        }
    }
}
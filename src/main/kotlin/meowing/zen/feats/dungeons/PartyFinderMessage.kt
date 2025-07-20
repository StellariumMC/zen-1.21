package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.text.Text
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style

@Zen.Module
object PartyFinderMessage : Feature("partyfindermsgs") {
    private val playerName get() = mc.player?.name?.string ?: ""
    private val joinedPattern = Regex("^Party Finder > (.+?) joined the dungeon group! \\((\\w+) Level (\\d+)\\)$")
    private val classSetPattern = Regex("^Party Finder > (.+?) set their class to (\\w+) Level (\\d+)!$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Party finder", ConfigElement(
                "partyfindermsgs",
                "Party finder messages",
                "Custom party finder messages.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.message.string.removeFormatting()

            when {
                text == "Party Finder > Your party has been queued in the dungeon finder!" -> {
                    event.cancel()
                    ChatUtils.addMessage("§c§lParty finder §7> §fParty queued.")
                }

                text == "Party Finder > Your group has been de-listed!" -> {
                    event.cancel()
                    ChatUtils.addMessage("§c§lParty finder §7> §fParty delisted.")
                }

                joinedPattern.matches(text) -> {
                    event.cancel()
                    val (user, cls, lvl) = joinedPattern.find(text)!!.destructured

                    if (user == playerName) {
                        ChatUtils.addMessage("§c§lParty finder §7> §b$user §8| §b$cls §7- §b$lvl")
                    } else {
                        val player = mc.player ?: return@register

                        val base = Text.literal("§c§lParty finder §7> §b$user §8| §b$cls §7- §b$lvl")

                        base.append(Text.literal(" §8| "))

                        val kickButton = Text.literal("§a[✖]").setStyle(
                            Style.EMPTY
                                .withClickEvent(ClickEvent.RunCommand("/p kick $user"))
                                .withHoverEvent(HoverEvent.ShowText(Text.literal("§cKick §b$user")))
                        )
                        base.append(kickButton)

                        base.append(Text.literal(" §8| "))

                        val pvButton = Text.literal("§a[PV]").setStyle(
                            Style.EMPTY
                                .withClickEvent(ClickEvent.RunCommand("/pv $user"))
                                .withHoverEvent(HoverEvent.ShowText(Text.literal("§cPV §b$user")))
                        )
                        base.append(pvButton)

                        player.sendMessage(base, false)
                    }
                }

                classSetPattern.matches(text) -> {
                    event.cancel()
                    val (user, cls, lvl) = classSetPattern.find(text)!!.destructured
                    ChatUtils.addMessage("§c§lParty finder §7> §b$user §fchanged to §b$cls §7- §b$lvl")
                }
            }
        }
    }
}
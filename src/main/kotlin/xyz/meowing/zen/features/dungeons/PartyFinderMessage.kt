package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object PartyFinderMessage : Feature("partyfindermsgs") {
    private val joinedPattern = Regex("^Party Finder > (.+?) joined the dungeon group! \\((\\w+) Level (\\d+)\\)$")
    private val classSetPattern = Regex("^Party Finder > (.+?) set their class to (\\w+) Level (\\d+)!$")

    override fun addConfig() {
        ConfigManager
            .addFeature("Custom PF Messages", "", "Dungeons", ConfigElement(
                "partyfindermsgs",
                ElementType.Switch(false)
            ))
    }


    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register

            val text = event.message.string.removeFormatting()

            when {
                text == "Party Finder > Your party has been queued in the dungeon finder!" -> {
                    event.cancel()
                    KnitChat.fakeMessage("§c§lParty finder §7> §fParty queued.")
                }

                text == "Party Finder > Your group has been de-listed!" -> {
                    event.cancel()
                    KnitChat.fakeMessage("§c§lParty finder §7> §fParty delisted.")
                }

                joinedPattern.matches(text) -> {
                    event.cancel()
                    val (user, cls, lvl) = joinedPattern.find(text)!!.destructured

                    if (user == player?.name?.string) {
                        KnitChat.fakeMessage("§c§lParty finder §7> §b$user §8| §b$cls §7- §b$lvl")
                    } else {
                        val base = KnitText.literal("§c§lParty finder §7> §b$user §8| §b$cls §7- §b$lvl")
                            .append(KnitText.literal(" §8| "))
                            .append(
                                KnitText.literal("[✖]")
                                    .green()
                                    .runCommand("/p kick $user")
                                    .onHover(KnitText.fromFormatted("§cKick §b$user"))
                            )
                            .append(KnitText.literal(" §8| "))
                            .append(
                                KnitText.literal("[PV]")
                                    .green()
                                    .runCommand("/pv $user")
                                    .onHover(KnitText.fromFormatted("§cPV §b$user"))
                            )
                            .toVanilla()

                        KnitChat.fakeMessage(base)
                    }
                }

                classSetPattern.matches(text) -> {
                    event.cancel()
                    val (user, cls, lvl) = classSetPattern.find(text)!!.destructured
                    KnitChat.fakeMessage("§c§lParty finder §7> §b$user §fchanged to §b$cls §7- §b$lvl")
                }
            }
        }
    }
}
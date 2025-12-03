package xyz.meowing.zen.features.dungeons

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.TickEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Module
object ServerLagTimer : Feature(
    "serverLagTimer",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private val regex = Pattern.compile("^\\s*☠ Defeated .+ in 0?[\\dhms ]+?\\s*(?:\\(NEW RECORD!\\))?$")
    private var sent = false
    private var ticking = false
    private var clientTick: Long = 0
    private var serverTick: Long = 0

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Server lag timer",
                "Shows the total time the server lagged for in Dungeons.",
                "Dungeons",
                ConfigElement(
                    "serverLagTimer",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register

            val text = event.message.string.removeFormatting()
            when {
                text == "[NPC] Mort: Good luck." -> {
                    ticking = true
                    sent = false
                }
                regex.matcher(text).matches() && !sent -> {
                    val lagtick = clientTick - serverTick
                    val lagtime = lagtick / 20.0
                    ticking = false
                    sent = true
                    TickScheduler.Client.schedule(2) {
                        KnitChat.fakeMessage("$prefix §fServer lagged for §c${"%.1f".format(lagtime)}s §7| §c${lagtick} ticks§f.")
                    }
                }
                else -> {}
            }
        }

        register<TickEvent.Server> {
            if (ticking) serverTick++
        }

        register<TickEvent.Client> {
            if (ticking) clientTick++
        }
    }

    override fun onRegister() {
        sent = false
        clientTick = 0
        serverTick = 0
        ticking = false
        super.onRegister()
    }

    override fun onUnregister() {
        sent = false
        clientTick = 0
        serverTick = 0
        ticking = false
        super.onUnregister()
    }
}

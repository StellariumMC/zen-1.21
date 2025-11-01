package xyz.meowing.zen.features.hud

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.PartyTracker
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.PartyEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object PartyDisplay : Feature("partydisplay") {
    private const val name = "Party Display"
    private var partyMembers = mapOf<String, PartyTracker.PartyMember>()

    override fun addConfig() {
        ConfigManager
            .addFeature("Party Display HUD", "", "HUD", ConfigElement(
                "partydisplay",
                ElementType.Switch(false)
            ))
    }


    override fun initialize() {
        HUDManager.register(name, "§9§lParty Members §r§7(5)\n §e• §3MrFast §6♚\n §e• §3MrFast §e(Archer 20)\n §e• §3MrFast §e(Mage 20)\n §e• §3MrFast §e(Berserker 20)\n §e• §3MrFast §e(Tank 20)")

        register<PartyEvent.Changed> { event ->
            partyMembers = event.members
        }

        register<GuiEvent.Render.HUD> { event ->
            if (HUDManager.isEnabled(name)) render(event.context)
        }
    }

    private fun render(context: DrawContext) {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        val lines = getDisplayLines()

        lines.forEachIndexed { index, line ->
            Render2D.renderStringWithShadow(context,line, x, y + (index * 10 * scale), scale)
        }
    }

    private fun getDisplayLines(): List<String> {
        if (partyMembers.isEmpty()) return emptyList()

        val playerName = Utils.currentPlayerName

        if (partyMembers.size == 1 && partyMembers.keys.contains(playerName)) return emptyList()

        val lines = mutableListOf<String>()
        lines.add("§9§lParty Members §r§7(${partyMembers.size})")

        for (partyMember in partyMembers.values) {
            val leaderText = if (partyMember.leader) " §6♚" else ""
            var name = partyMember.name
            if (partyMember.name == player?.name?.string) name = "§a${partyMember.name}"
            var line = " §e• §3${name}$leaderText"
            if (partyMember.className.isNotEmpty()) line += " §e(${partyMember.className} ${partyMember.classLvl})"
            lines.add(line)
        }
        return lines
    }
}
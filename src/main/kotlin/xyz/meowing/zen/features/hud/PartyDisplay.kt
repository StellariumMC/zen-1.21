package xyz.meowing.zen.features.hud

import xyz.meowing.zen.api.hypixel.PartyTracker
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.PartyEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object PartyDisplay : Feature(
    "partyDisplay"
) {
    private const val NAME = "Party Display"
    private var partyMembers = mapOf<String, PartyTracker.PartyMember>()

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Party display HUD",
                "Display party members on HUD",
                "HUD",
                ConfigElement(
                    "partyDisplay",
                    ElementType.Switch(false)
                )
            )
    }


    override fun initialize() {
        HUDManager.register(
            NAME,
            "§9§lParty Members §r§7(5)\n §e• §3MrFast §6♚\n §e• §3MrFast §e(Archer 20)\n §e• §3MrFast §e(Mage 20)\n §e• §3MrFast §e(Berserker 20)\n §e• §3MrFast §e(Tank 20)",
            "partyDisplay"
        )

        register<PartyEvent.Changed> { event ->
            partyMembers = event.members
        }

        register<GuiEvent.Render.HUD.Pre> { event ->
            render(event.context)
        }
    }

    private fun render(context: GuiGraphics) {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)
        val lines = getDisplayLines()

        lines.forEachIndexed { index, line ->
            Render2D.renderStringWithShadow(context,line, x, y + (index * 10 * scale), scale)
        }
    }

    private fun getDisplayLines(): List<String> {
        if (partyMembers.isEmpty()) return emptyList()
        if (partyMembers.size == 1 && partyMembers.keys.contains(player?.name?.string)) return emptyList()

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
package xyz.meowing.zen.features.slayers

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ConfigManager
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern
import xyz.meowing.zen.config.ConfigElement

@Zen.Module
object VengDamage : Feature("vengdmg", true) {
    private var nametagID = -1
    private val veng = Pattern.compile("^\\d+(,\\d+)*ﬗ$")

    override fun addConfig() {
        ConfigManager
            .addFeature("Vengeance damage tracker", "Vengeance damager tracker", "Slayers", ConfigElement(
                    "vengdmg",
                    ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<SkyblockEvent.Slayer.Spawn> { event ->
            nametagID = event.entityID
        }

        register<SkyblockEvent.DamageSplash> { event ->
            if (nametagID == -1) return@register

            val entityName = event.originalName.removeFormatting()
            val vengMatch = veng.matcher(entityName)
            if (!vengMatch.matches()) return@register
            val name = vengMatch.group(0).replace("ﬗ", "")

            val nametagEntity = world?.getEntityById(nametagID) ?: return@register
            if (event.entity.distanceTo(nametagEntity) > 5) return@register

            val numStr = name.replace(",", "")
            val num = numStr.toLongOrNull() ?: return@register

            if (num > 500000) KnitChat.fakeMessage("$prefix §fVeng DMG: §c${name}")
        }
    }
}
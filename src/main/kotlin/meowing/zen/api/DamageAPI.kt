package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.EventBus.post
import meowing.zen.events.SkyblockEvent
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object DamageAPI {
    private val damageRegex = "^[✧✯]?(\\d{1,3}(?:,\\d{3})*)[⚔+✧❤♞☄✷ﬗ✯]*$".toRegex()

    init {
        EventBus.register<EntityEvent.Metadata> { event ->
            val packet = event.packet
            val entity = event.entity
            val name = event.name

            val matchResult = damageRegex.find(name.removeFormatting()) ?: return@register
            val damageStr = matchResult.groupValues[1].replace(",", "")
            val damage = damageStr.toIntOrNull() ?: return@register

            if (post(SkyblockEvent.DamageSplash(damage, name, entity.pos, packet, entity))) event.cancel()
        }
    }
}
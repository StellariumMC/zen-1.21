package xyz.meowing.zen.api

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.EventBus.post
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.utils.Utils.removeFormatting

@Module
object DamageAPI {
    private val damageRegex = "^[✧✯]?(\\d{1,3}(?:,\\d{3})*)[⚔+✧❤♞☄✷ﬗ✯]*$".toRegex()

    init {
        EventBus.register<EntityEvent.Packet.Metadata> { event ->
            val packet = event.packet
            val entity = event.entity
            val name = event.name

            val matchResult = damageRegex.find(name.removeFormatting()) ?: return@register
            val damageStr = matchResult.groupValues[1].replace(",", "")
            val damage = damageStr.toIntOrNull() ?: return@register

            if (post(
                    SkyblockEvent.DamageSplash(
                    damage,
                    name,
                    //#if MC >= 1.21.9
                    //$$ entity.entityPos,
                    //#else
                    entity.pos,
                    //#endif
                    packet,
                    entity
            ))) event.cancel()
        }
    }
}
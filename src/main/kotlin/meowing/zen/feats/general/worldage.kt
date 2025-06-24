package meowing.zen.feats.general

import meowing.zen.events.EventBus
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldChangeEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.Zen.Companion.mc

object worldage : Feature("worldage") {
    private var tickCall: EventBus.EventCall? = null

    override fun initialize() {
        register<WorldChangeEvent> {
            tickCall?.unregister()

            tickCall = EventBus.register<TickEvent>({ _ ->
                if (mc.world == null) return@register

                val daysRaw = mc.world!!.time / 24000.0
                val days = if (daysRaw % 1.0 == 0.0) daysRaw.toInt().toString() else "%.1f".format(daysRaw)

                ChatUtils.addMessage("§c[Zen] §fWorld is §b$days §fdays old.")
                tickCall?.unregister()
                tickCall = null
            })
        }
    }
}
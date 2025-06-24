package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EntityJoinEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

object vengdmg : Feature("vengdmg") {
    private var nametagID = -1
    private val veng = Pattern.compile("^\\d+(,\\d+)*ﬗ$")
    fun handleNametagUpdate(entityId: Int) { nametagID = entityId }

    override fun initialize() {
        register<EntityJoinEvent> { event ->
            if (nametagID == -1) return@register
            TickUtils.schedule(2) {
                val entityName = event.entity.name.string?.removeFormatting() ?: return@schedule
                val vengMatch = veng.matcher(entityName)
                if (vengMatch.matches()) {
                    val spawnedEntity = mc.world?.getEntityById(event.entity.id) ?: return@schedule
                    val nametagEntity = mc.world?.getEntityById(nametagID) ?: return@schedule

                    if (spawnedEntity.distanceTo(nametagEntity) <= 5) {
                        val numStr = vengMatch.group(0).replace("ﬗ", "").replace(",", "")
                        numStr.toLongOrNull()?.let { num ->
                            if (num > 500000) ChatUtils.addMessage("§c[Zen] §fVeng DMG: §c${vengMatch.group(0).replace("ﬗ", "")}")
                        }
                    }
                }
            }
        }
    }
}
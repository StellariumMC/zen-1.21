package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ConfigDelegate
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.text.Text
import java.util.Optional

@Zen.Module
object MetadataHandler {
    private val slayertimer by ConfigDelegate<Boolean>("slayertimer")
    private val vengdmg by ConfigDelegate<Boolean>("vengdmg")
    private val lasertimer by ConfigDelegate<Boolean>("lasertimer")

    init {
        EventBus.register<EntityEvent.Metadata> ({ event ->
            if (!slayertimer && !vengdmg && !lasertimer) return@register
            val world = mc.world ?: return@register
            val player = mc.player ?: return@register

            val packet = event.packet
            packet.trackedValues?.find { it.id == 2 && it.value is Optional<*> }?.let { obj ->
                val optional = obj.value as Optional<*>
                val name = (optional.orElse(null) as? Text)?.string?.removeFormatting() ?: return@let
                if (name.contains("Spawned by") && name.endsWith("by: ${player.gameProfile.name}")) {
                    val targetEntity = world.getEntityById(packet.id)

                    val hasBlackhole = targetEntity?.let { entity ->
                        world.entities.any { Entity ->
                            entity.distanceTo(Entity) <= 3f && Entity.customName?.string?.removeFormatting()?.lowercase()?.contains("black hole") == true
                        }
                    } ?: false

                    if (hasBlackhole) return@register
                    if (slayertimer) SlayerTimer.handleBossSpawn(packet.id)
                    if (vengdmg) VengDamage.handleNametagUpdate(packet.id)
                    if (lasertimer) LaserTimer.handleSpawn(packet.id)
                }
            }
        })
    }
}
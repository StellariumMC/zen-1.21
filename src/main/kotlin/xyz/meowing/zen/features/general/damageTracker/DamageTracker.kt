package xyz.meowing.zen.features.general.damageTracker

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.player
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigManager.configUI
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.text.DecimalFormat

@Module
object DamageTracker : Feature(
    "damageTracker",
    "Damage tracker",
    "Track damage dealt to mobs\n§7This does not track the damage done by arrows shot using duplex or the extra arrows from Terminator",
    "General",
    skyblockOnly = true
) {
    val stats = DamageStats()
    private val formatter = DecimalFormat("#,###")
    private var lastHitEntity: Entity? = null
    private var lastHitTime = 0L

    private val hitTypes by config.multiCheckbox("Hit types", DamageType.entries.map { it.displayName })
    private val showInChat by config.switch("Show in chat")

    override fun initialize() {
        updateEnabledTypes()

        configUI.registerListener("damageTracker.hitTypes") {
            updateEnabledTypes()
        }

        register<EntityEvent.Attack> { event ->
            val player = player ?: return@register
            if (event.player.name.string != player.name.string) return@register

            lastHitEntity = event.target
            lastHitTime = System.currentTimeMillis()
        }

        register<EntityEvent.ArrowHit> { event ->
            val player = player ?: return@register
            if (event.shooterName != player.name.string) return@register

            lastHitEntity = event.hitEntity
        }

        register<SkyblockEvent.DamageSplash> { event ->
            val lastHit = lastHitEntity ?: return@register

            val hitEntityPos = Vec3(lastHit.x, lastHit.y + lastHit.bbHeight / 2, lastHit.z)
            val distance = event.entityPos.distanceTo(hitEntityPos)

            if (distance > 3.0) return@register

            val type = detectDamageType(event.originalName, event.originalName.removeFormatting())
            if (!stats.enabledTypes.contains(type)) return@register

            stats.entries.add(DamageEntry(event.damage, type))
            if (stats.entries.size > 1000) stats.entries.removeAt(0)

            if (showInChat) {
                val formattedDamage = formatter.format(event.damage)
                val message = "${type.chatColor}${type.symbol} §r${type.chatColor}$formattedDamage §8[${type.displayName}]"
                KnitChat.fakeMessage("$prefix $message")
            }
        }
    }

    private fun updateEnabledTypes() {
        stats.enabledTypes.clear()
        hitTypes.forEach { index ->
            if (index < DamageType.entries.size) stats.enabledTypes.add(DamageType.entries[index])
        }
    }

    private fun detectDamageType(originalName: String, cleanName: String): DamageType {
        return when {
            cleanName.contains("✧") -> DamageType.CRIT
            cleanName.contains("✯") -> DamageType.OVERLOAD
            originalName.contains("§6") -> DamageType.FIRE
            else -> DamageType.NORMAL
        }
    }

    fun getStats(type: DamageType? = null): Triple<Int, Int, Double> {
        val filteredEntries = if (type != null) {
            stats.entries.filter { it.type == type }
        } else {
            stats.entries
        }

        if (filteredEntries.isEmpty()) return Triple(0, 0, 0.0)

        val total = filteredEntries.sumOf { it.damage }
        val max = filteredEntries.maxOf { it.damage }
        val avg = total.toDouble() / filteredEntries.size

        return Triple(total, max, avg)
    }

    fun clearStats() {
        stats.entries.clear()
    }
}
package xyz.meowing.zen.features.general.damageTracker

data class DamageEntry(
    val damage: Int,
    val type: DamageType,
    val timestamp: Long = System.currentTimeMillis()
)
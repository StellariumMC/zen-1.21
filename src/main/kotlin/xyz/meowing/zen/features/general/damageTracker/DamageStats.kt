package xyz.meowing.zen.features.general.damageTracker

data class DamageStats(
    val entries: MutableList<DamageEntry> = mutableListOf(),
    var enabledTypes: MutableSet<DamageType> = mutableSetOf(DamageType.CRIT)
)
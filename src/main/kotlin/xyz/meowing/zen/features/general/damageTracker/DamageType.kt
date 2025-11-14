package xyz.meowing.zen.features.general.damageTracker

import java.awt.Color

enum class DamageType(
    val displayName: String,
    val symbol: String,
    val chatColor: String,
    val guiColor: Color
) {
    CRIT(
        "Crit Hits",
        "✧",
        "§b§l",
        Color(85, 170, 255, 255)
    ),
    OVERLOAD(
        "Overload Hits",
        "✯",
        "§d§l",
        Color(255, 85, 255, 255)
    ),
    FIRE(
        "Fire Hits",
        "🔥",
        "§6§l",
        Color(255, 170, 0, 255)
    ),
    NORMAL(
        "Non-Crit Hits",
        "⚔",
        "§f",
        Color(200, 200, 200, 255)
    )
    ;
}

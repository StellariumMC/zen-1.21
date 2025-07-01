package meowing.zen.config

import meowing.zen.config.ui.ConfigUI
import java.awt.Color

class ConfigAccessor(val configUI: ConfigUI) {
    val carrycounter: Boolean get() = configUI.getConfigValue("carrycounter") as? Boolean ?: false
    val carryvalue: String get() = configUI.getConfigValue("carryvalue") as? String ?: "1.3"
    val carrybosshighlight: Boolean get() = configUI.getConfigValue("carrybosshighlight") as? Boolean ?: false
    val carrybosshighlightcolor: Color get() = configUI.getConfigValue("carrybosshighlightcolor") as? Color ?: Color(0, 255, 255, 127)
    val carryclienthighlight: Boolean get() = configUI.getConfigValue("carryclienthighlight") as? Boolean ?: false
    val carryclienthighlightcolor: Color get() = configUI.getConfigValue("carryclienthighlightcolor") as? Color ?: Color(0, 255, 255, 127)
    val slayertimer: Boolean get() = configUI.getConfigValue("slayertimer") as? Boolean ?: false
    val slayerhighlightcolor: Color get() = configUI.getConfigValue("slayerhighlightcolor") as? Color ?: Color(0, 255, 255, 127)
    val vengdmg: Boolean get() = configUI.getConfigValue("vengdmg") as? Boolean ?: false
    val slayerstats: Boolean get() = configUI.getConfigValue("slayerstats") as? Boolean ?: false
    val blockoverlaycolor: Color get() = configUI.getConfigValue("blockoverlaycolor") as? Color ?: Color(0, 255, 255, 127)
    val blockoverlaywidth: Double get() = configUI.getConfigValue("blockoverlaywidth") as? Double ?: 2.0
    val cryptreminderdelay: Double get() = configUI.getConfigValue("cryptreminderdelay") as? Double ?: 2.0
    val carrycountsend: Boolean get() = configUI.getConfigValue("carrycountsend") as? Boolean ?: false

    fun getValue(key: String): Any? = configUI.getConfigValue(key)

    inline fun <reified T> getValue(key: String, default: T): T {
        return configUI.getConfigValue(key) as? T ?: default
    }
}
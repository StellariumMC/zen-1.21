package meowing.zen.config

import meowing.zen.config.ui.ConfigUI
import java.awt.Color

class ConfigAccessor(val configUI: ConfigUI) {
    val carrycounter: Boolean get() = configUI.getConfigValue("carrycounter") as? Boolean ?: false
    val carryvalue: String get() = configUI.getConfigValue("carryvalue") as? String ?: "1.3"
    val carrybosshighlight: Boolean get() = configUI.getConfigValue("carrybosshighlight") as? Boolean ?: false
    val carrybosshighlightcolor: Color get() = configUI.getColorValue("carrybosshighlightcolor") ?: Color(0, 255, 255, 127)
    val carryclienthighlight: Boolean get() = configUI.getConfigValue("carryclienthighlight") as? Boolean ?: false
    val carryclienthighlightcolor: Color get() = configUI.getColorValue("carryclienthighlightcolor") ?: Color(0, 255, 255, 127)
    val slayertimer: Boolean get() = configUI.getConfigValue("slayertimer") as? Boolean ?: false
    val vengdmg: Boolean get() = configUI.getConfigValue("vengdmg") as? Boolean ?: false
    val lasertimer: Boolean get() = configUI.getConfigValue("lasertimer") as? Boolean ?: false
    val slayerstats: Boolean get() = configUI.getConfigValue("slayerstats") as? Boolean ?: false
    val cryptreminderdelay: Double get() = configUI.getConfigValue("cryptreminderdelay") as? Double ?: 2.0
    val carrycountsend: Boolean get() = configUI.getConfigValue("carrycountsend") as? Boolean ?: false
    val draftself: Boolean get() = configUI.getConfigValue("draftself") as? Boolean ?: false
    val autogetdraft: Boolean get() = configUI.getConfigValue("autogetdraft") as? Boolean ?: false
    val leapmessage: String get() = configUI.getConfigValue("leapmessage") as? String ?: "Leaping to"

    fun getValue(key: String): Any? = configUI.getConfigValue(key)

    inline fun <reified T> getValue(key: String, default: T): T {
        return configUI.getConfigValue(key) as? T ?: default
    }
}
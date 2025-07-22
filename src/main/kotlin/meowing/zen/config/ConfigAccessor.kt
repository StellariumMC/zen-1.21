package meowing.zen.config

import meowing.zen.config.ui.ConfigUI
import org.lwjgl.glfw.GLFW
import java.awt.Color

class ConfigAccessor(val configUI: ConfigUI) {
    private var _carrycounter = false
    private var _carryvalue = "1.3"
    private var _carrybosshighlight = false
    private var _carrybosshighlightcolor = Color(0, 255, 255, 127)
    private var _carryclienthighlight = false
    private var _carryclienthighlightcolor = Color(0, 255, 255, 127)
    private var _slayertimer = false
    private var _vengdmg = false
    private var _lasertimer = false
    private var _slayerstats = false
    private var _cryptreminderdelay = 2.0
    private var _carrycountsend = false
    private var _draftself = false
    private var _autogetdraft = false
    private var _leapmessage = "Leaping to"
    private var _boxstarmobscolor = Color(0, 255, 255, 127)
    private var _keyhighlightcolor = Color(0, 255, 255, 127)
    private var _blockoverlaycolor = Color(255, 255, 255, 255)
    private var _customX = 1.0f
    private var _customY = 1.0f
    private var _customZ = 1.0f
    private var _customself = false
    private var _slayerhighlightcolor = Color(0, 255, 255, 127)
    private var _slayerhighlightfilled = false
    private var _entityhighlightplayercolor = Color(0, 255, 255, 255)
    private var _entityhighlightmobcolor = Color(255, 0, 0, 255)
    private var _entityhighlightanimalcolor = Color(0, 255, 0, 255)
    private var _entityhighlightothercolor = Color(255, 255, 255, 255)
    private var _highlightlividcolor = Color(0, 255, 255, 127)
    private var _hidewronglivid = false
    private var _highlightlividline = false
    private var _ragparty = false
    private var _armorhudvert = false
    private var _firefreezeoverlaycolor = Color(0, 255, 255, 127)
    private var _chatcleanerkey = GLFW.GLFW_KEY_H
    private var _customtintcolor = Color(0, 255, 255, 127)
    private var _carrysendmsg = false
    private var _berberishelpercolor = Color(0, 255, 255, 127)
    private var _effectiveareaoverlaycolor = Color(0, 255, 255, 127)

    val carrycounter get() = _carrycounter
    val carryvalue get() = _carryvalue
    val carrybosshighlight get() = _carrybosshighlight
    val carrybosshighlightcolor get() = _carrybosshighlightcolor
    val carryclienthighlight get() = _carryclienthighlight
    val carryclienthighlightcolor get() = _carryclienthighlightcolor
    val slayertimer get() = _slayertimer
    val vengdmg get() = _vengdmg
    val lasertimer get() = _lasertimer
    val slayerstats get() = _slayerstats
    val cryptreminderdelay get() = _cryptreminderdelay
    val carrycountsend get() = _carrycountsend
    val draftself get() = _draftself
    val autogetdraft get() = _autogetdraft
    val leapmessage get() = _leapmessage
    val boxstarmobscolor get() = _boxstarmobscolor
    val keyhighlightcolor get() = _keyhighlightcolor
    val blockoverlaycolor get() = _blockoverlaycolor
    val customX get() = _customX
    val customY get() = _customY
    val customZ get() = _customZ
    val customself get() = _customself
    val slayerhighlightcolor get() = _slayerhighlightcolor
    val entityhighlightplayercolor get() = _entityhighlightplayercolor
    val entityhighlightmobcolor get() = _entityhighlightmobcolor
    val entityhighlightanimalcolor get() = _entityhighlightanimalcolor
    val entityhighlightothercolor get() = _entityhighlightothercolor
    val highlightlividcolor get() = _highlightlividcolor
    val hidewronglivid get() = _hidewronglivid
    val highlightlividline get() = _highlightlividline
    val ragparty get() = _ragparty
    val armorhudvert get() = _armorhudvert
    val firefreezeoverlaycolor get() = _firefreezeoverlaycolor
    val chatcleanerkey get() = _chatcleanerkey
    val customtintcolor get() = _customtintcolor
    val carrysendmsg get() = _carrysendmsg
    val berberishelpercolor get() = _berberishelpercolor
    val effectiveareaoverlaycolor get() = _effectiveareaoverlaycolor

    init {
        configUI
            .registerListener("carrycounter") { _carrycounter = it as Boolean }
            .registerListener("carryvalue") { _carryvalue = it as String }
            .registerListener("carrybosshighlight") { _carrybosshighlight = it as Boolean }
            .registerListener("carrybosshighlightcolor") { _carrybosshighlightcolor = it as Color }
            .registerListener("carryclienthighlight") { _carryclienthighlight = it as Boolean }
            .registerListener("carryclienthighlightcolor") { _carryclienthighlightcolor = it as Color }
            .registerListener("slayertimer") { _slayertimer = it as Boolean }
            .registerListener("vengdmg") { _vengdmg = it as Boolean }
            .registerListener("lasertimer") { _lasertimer = it as Boolean }
            .registerListener("slayerstats") { _slayerstats = it as Boolean }
            .registerListener("cryptreminderdelay") { _cryptreminderdelay = it as Double }
            .registerListener("carrycountsend") { _carrycountsend = it as Boolean }
            .registerListener("draftself") { _draftself = it as Boolean }
            .registerListener("autogetdraft") { _autogetdraft = it as Boolean }
            .registerListener("leapmessage") { _leapmessage = it as String }
            .registerListener("boxstarmobscolor") { _boxstarmobscolor = it as Color }
            .registerListener("keyhighlightcolor") { _keyhighlightcolor = it as Color }
            .registerListener("blockoverlaycolor") { _blockoverlaycolor = it as Color }
            .registerListener("customX") { _customX = (it as Double).toFloat() }
            .registerListener("customY") { _customY = (it as Double).toFloat() }
            .registerListener("customZ") { _customZ = (it as Double).toFloat() }
            .registerListener("customself") { _customself = it as Boolean }
            .registerListener("slayerhighlightcolor") { _slayerhighlightcolor = it as Color }
            .registerListener("slayerhighlightfilled") { _slayerhighlightfilled = it as Boolean }
            .registerListener("entityhighlightplayercolor") { _entityhighlightplayercolor = it as Color }
            .registerListener("entityhighlightmobcolor") { _entityhighlightmobcolor = it as Color }
            .registerListener("entityhighlightanimalcolor") { _entityhighlightanimalcolor = it as Color }
            .registerListener("entityhighlightothercolor") { _entityhighlightothercolor = it as Color }
            .registerListener("highlightlividcolor") { _highlightlividcolor = it as Color }
            .registerListener("hidewronglivid") { _hidewronglivid = it as Boolean }
            .registerListener("highlightlividline") { _highlightlividline = it as Boolean }
            .registerListener("ragparty") { _ragparty = it as Boolean }
            .registerListener("armorhudvert") { _armorhudvert = it as Boolean }
            .registerListener("firefreezeoverlaycolor") { _firefreezeoverlaycolor = it as Color }
            .registerListener("chatcleanerkey") { _chatcleanerkey = (it as Number).toInt() }
            .registerListener("customtintcolor") { _customtintcolor = it as Color }
            .registerListener("carrysendmsg") { _carrysendmsg = it as Boolean }
            .registerListener("berberishelpercolor") { _berberishelpercolor = it as Color }
            .registerListener("effectiveareaoverlaycolor") { _effectiveareaoverlaycolor = it as Color }
    }

    fun getValue(key: String): Any? = configUI.getConfigValue(key)
    inline fun <reified T> getValue(key: String, default: T): T = configUI.getConfigValue(key) as? T ?: default
}
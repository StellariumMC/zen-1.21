package xyz.meowing.zen.config.ui

import xyz.meowing.knit.api.render.KnitResolution
import xyz.meowing.vexel.core.VexelScreen
import xyz.meowing.zen.config.ui.elements.base.ConfigValidator
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.config.ui.panels.Panel
import xyz.meowing.zen.config.ui.elements.MCColorCode
import xyz.meowing.zen.managers.config.CategoryElement
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.Utils.toColorFromMap
import java.awt.Color

typealias ConfigData = Map<String, Any>

class ClickGUI : VexelScreen("Zen Config") {
    private val validator = ConfigValidator()
    private val panels = mutableListOf<Panel>()

    private val categoryOrder = listOf(
        "general", "qol", "hud", "dungeons", "slayers", "visuals", "meowing", "rift", "mining"
    )

    private lateinit var searchBar: SearchBar

    private val configListeners = mutableMapOf<String, MutableList<(Any) -> Unit>>()
    private val closeListeners = mutableListOf<() -> Unit>()
    private val elementRefs = mutableMapOf<String, ConfigElement>()

    override fun afterInitialization() {
        panels.clear()

        val sortedCategories = ConfigManager.configTree.sortedWith(
            compareBy<CategoryElement> { cat ->
                categoryOrder.indexOf(cat.name.lowercase()).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }.thenBy { it.name }
        )

        sortedCategories.forEachIndexed { index, category ->
            val categoryName = category.name.lowercase()

            if (categoryName == "debug") {
                val panel = Panel(category, 50f, KnitResolution.windowHeight - 400f, ::updateConfig)
                panel.childOf(window)
                panels.add(panel)
                return@forEachIndexed
            }

            val col = index % 7
            val row = index / 7

            val x = if (row > 0) 50f + (col + 1) * 260f else 50f + col * 260f
            val y = 50f + row * 400f

            val panel = Panel(category, x, y, ::updateConfig)
            panel.childOf(window)
            panels.add(panel)
        }

        searchBar = SearchBar { query ->
            filterPanels(query)
        }
        searchBar.childOf(window)
    }

    private fun getDefaultValue(type: ElementType?): Any? = when (type) {
        is ElementType.Switch -> type.default
        is ElementType.Slider -> type.default
        is ElementType.Dropdown -> type.default
        is ElementType.TextInput -> type.default
        is ElementType.ColorPicker -> type.default
        is ElementType.Keybind -> type.default
        is ElementType.MultiCheckbox -> type.default
        is ElementType.MCColorPicker -> type.default
        else -> null
    }

    fun updateConfig(configKey: String, newValue: Any) {
        val validatedValue = validator.validate(configKey, newValue) ?: return

        val serializedValue = when (validatedValue) {
            is Color -> mapOf(
                "r" to validatedValue.red,
                "g" to validatedValue.green,
                "b" to validatedValue.blue,
                "a" to validatedValue.alpha
            )
            is Set<*> -> validatedValue.toList()
            is MCColorCode -> validatedValue.code
            else -> validatedValue
        }

        ConfigManager.configValueMap[configKey] = serializedValue
        ConfigManager.saveConfig(false)

        configListeners[configKey]?.forEach { it(validatedValue) }
    }

    fun registerListener(configKey: String, listener: (Any) -> Unit): ClickGUI {
        configListeners.getOrPut(configKey) { mutableListOf() }.add(listener)

        (ConfigManager.getConfigValue(configKey) ?: getDefaultValue(elementRefs[configKey]?.type))?.let { currentValue ->
            val resolvedValue = when (currentValue) {
                is Map<*, *> -> currentValue.toColorFromMap()
                is List<*> -> currentValue.mapNotNull { (it as? Number)?.toInt() }.toSet()
                else -> currentValue
            }
            resolvedValue?.let { listener(it) }
        }
        return this
    }

    fun registerCloseListener(listener: () -> Unit): ClickGUI {
        closeListeners.add(listener)
        return this
    }

    fun getConfigValue(configKey: String): Any? {
        val value = ConfigManager.getConfigValue(configKey) ?: return null
        return when (value) {
            is Map<*, *> -> value.toColorFromMap()
            is List<*> -> value.mapNotNull { (it as? Number)?.toInt() }.toSet()
            else -> value
        }
    }

    private fun filterPanels(query: String) {
        if (query.isEmpty()) {
            panels.forEach { it.visible = true }
        } else {
            panels.forEach { panel ->
                panel.visible = panel.matchesSearch(query)
            }
        }
    }

    override fun onCloseGui() {
        ConfigManager.saveConfig(true)
        closeListeners.forEach { it() }
        super.onCloseGui()
    }
}
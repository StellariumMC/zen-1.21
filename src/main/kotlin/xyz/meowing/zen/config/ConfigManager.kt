package xyz.meowing.zen.config

import xyz.meowing.zen.config.ui.ConfigData
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.Utils.toColorFromMap

//override fun addConfig(configUI: ConfigUI): ConfigUI {
//    return configUI
//        .addFeature("Architect Draft Message","", "Dungeons", ConfigElement(
//            "architectdraft",
//            null,
//            ElementType.Switch(false)
//        ), isSectionToggle = true)
//        .addFeatureOption("Only get drafts on your fails", "Options", ConfigElement(
//            "selfdraft",
//            ElementType.Switch(false)
//        ))
//        .addFeatureOption("Automatically get architect drafts", "Options", ConfigElement(
//            "autogetdraft",
//            ElementType.Switch(false)
//        ))
//}

object ConfigManager {
    private val dataUtils = DataUtils("config", mutableMapOf<String, Any>())
    val configValueMap: MutableMap<String, Any> = dataUtils.getData()

    private val categoryOrder = listOf("general", "qol", "hud", "visuals", "slayers", "dungeons", "meowing", "rift")
    val configTree = mutableListOf<CategoryElement>()

    fun addFeature(
        featureName: String,
        description: String,
        categoryName: String,
        element: ConfigElement
    ): FeatureElement {
        // Find or create category
        val category = configTree.firstOrNull { it.name.equals(categoryName, ignoreCase = true) } ?: CategoryElement(
            categoryName
        ).also { configTree.add(it) }

        // Sort categories by predefined order, then alphabetically
        configTree.sortWith(
            compareBy<CategoryElement> { cat ->
                categoryOrder.indexOf(cat.name.lowercase()).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }.thenBy { it.name }
        )

        // Create the actual config element (the feature itself)
        val featureElement = FeatureElement(
            featureName,
            description,
            element
        )
        // Set parent reference on the feature's ConfigElement
        featureElement.configElement.parent = featureElement

        if(!category.features.any { it.featureName == featureName }) {
            category.features.add(featureElement)
        }

        return featureElement
    }

    fun saveConfig() {
        dataUtils.setData(configValueMap)
    }

    fun getConfigValue(configKey: String): Any? {
        return when (val value = configValueMap[configKey]) {
            is Map<*, *> -> value.toColorFromMap()
            is List<*> -> value.mapNotNull { (it as? Number)?.toInt() }.toSet()
            else -> value
        }
    }
}

// Marker interface for containers of ConfigElement
interface ConfigContainer

class FeatureElement(
    val featureName: String,
    val description: String,
    val configElement: ConfigElement
) : ConfigContainer {
    val options: MutableMap<String, MutableList<OptionElement>> = mutableMapOf()

    fun addFeatureOption(
        optionName: String,
        description: String = "",
        optionsSection: String = "Options",
        element: ConfigElement
    ): FeatureElement {
        val option = OptionElement(optionName, description, optionsSection, element)
        // Wire parent reference for the option's ConfigElement
        option.configElement.parent = option
        option.configElement.value
        val optionsList = options.getOrPut(optionsSection) { mutableListOf() }
        if(!optionsList.any { it.optionName == optionName }) optionsList.add(option)

        return this
    }
}

class OptionElement(
    val optionName: String,
    val description: String = "",
    val optionsSection: String = "Options",
    val configElement: ConfigElement
) : ConfigContainer

data class ConfigElement(
    val configKey: String,
    val type: ElementType,
    val shouldShow: (ConfigData) -> Boolean = { true },
    val value: Any? = null,
) {
    var parent: ConfigContainer? = null
}

data class CategoryElement(val name: String) {
    val features: MutableList<FeatureElement> = mutableListOf()
}
package xyz.meowing.zen.ui

import xyz.meowing.zen.config.ui.ConfigData
import xyz.meowing.zen.config.ui.types.ElementType

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

object ConfigMenuManager {
    val categories = mutableListOf<CategoryElement>()
    private val categoryOrder = listOf("general", "qol", "hud", "visuals", "slayers", "dungeons", "meowing", "rift")

    fun addFeature(
        featureName: String,
        description: String,
        categoryName: String,
        element: ConfigElement
    ): FeatureElement {
        // Find or create category
        val category = categories.firstOrNull { it.name.equals(categoryName, ignoreCase = true) } ?: CategoryElement(
            categoryName
        ).also { categories.add(it) }

        // Sort categories by predefined order, then alphabetically
        categories.sortWith(
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

        if(!category.features.any { it.featureName == featureName }) {
            category.features.add(featureElement)
        }

        return featureElement
    }
}

class FeatureElement(
    val featureName: String,
    val description: String,
    val configElement: ConfigElement
) {
    val options: MutableMap<String, MutableList<OptionElement>> = mutableMapOf()

    fun addFeatureOption(
        optionName: String,
        description: String = "",
        optionsSection: String = "Options",
        element: ConfigElement
    ): FeatureElement {
        val option = OptionElement(optionName, description, optionsSection, element)
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
)

data class ConfigElement(
    val configKey: String,
    val type: ElementType,
    val shouldShow: (ConfigData) -> Boolean = { true }
)

data class CategoryElement(val name: String) {
    val features: MutableList<FeatureElement> = mutableListOf()
}
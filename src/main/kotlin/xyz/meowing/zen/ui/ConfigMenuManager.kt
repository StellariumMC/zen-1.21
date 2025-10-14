package xyz.meowing.zen.ui

import kotlinx.serialization.Serializable
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigData
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.ui.ConfigMenuManager.printCategoriesJson

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

    fun printCategoriesJson() {
        val json = buildString {
            append("[")
            categories.forEachIndexed { cIdx, category ->
                append("{\"name\":\"${category.name}\",\"features\":[")
                category.features.forEachIndexed { fIdx, feature ->
                    append("{\"featureName\":\"${feature.featureName}\",\"description\":\"${feature.description}\",\"options\":{")
                    feature.options.entries.forEachIndexed { oIdx, (section, options) ->
                        append("\"$section\":[")
                        options.forEachIndexed { i, option ->
                            append("{\"optionName\":\"${option.optionName}\",\"description\":\"${option.description}\",\"configKey\":\"${option.configElement.configKey}\"}")
                            if (i < options.size - 1) append(",")
                        }
                        append("]")
                        if (oIdx < feature.options.size - 1) append(",")
                    }
                    append("}}")
                    if (fIdx < category.features.size - 1) append(",")
                }
                append("]}")
                if (cIdx < categories.size - 1) append(",")
            }
            append("]")
        }
        println(json)
    }

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

@Serializable
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
        // Change config key to be like "feature.optionthing"
        element.configKey = this.configElement.configKey + "." + element.configKey

        val option = OptionElement(optionName, description, optionsSection, element)
        val optionsList = options.getOrPut(optionsSection) { mutableListOf() }
        if(!optionsList.any { it.optionName == optionName }) optionsList.add(option)

        return this
    }
}

@Serializable
class OptionElement(
    val optionName: String,
    val description: String = "",
    val optionsSection: String = "Options",
    val configElement: ConfigElement
)

@Serializable
data class ConfigElement(
    var configKey: String,
    val type: ElementType,
    val shouldShow: (ConfigData) -> Boolean = { true }
)

@Serializable
data class CategoryElement(val name: String) {
    val features: MutableList<FeatureElement> = mutableListOf()
}

@Zen.Command
object ConfigTestCommand : Commodore("configtest") {
    init {
        runs {
            printCategoriesJson()
        }
    }
}
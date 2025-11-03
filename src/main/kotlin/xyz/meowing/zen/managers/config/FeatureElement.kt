package xyz.meowing.zen.managers.config

class FeatureElement(
    val featureName: String,
    val description: String,
    val configElement: ConfigElement
) : ConfigContainer {
    val options: MutableMap<String, MutableList<OptionElement>> = mutableMapOf()

    fun addFeatureOption(
        optionName: String,
        element: ConfigElement
    ): FeatureElement {
        val option = OptionElement(optionName, element)
        val optionsSection = "Options"
        option.configElement.parent = option
        val optionsList = options.getOrPut(optionsSection) { mutableListOf() }
        if (!optionsList.any { it.optionName == optionName }) optionsList.add(option)

        ConfigManager.ensureDefaultValue(element)

        return this
    }
}
@file:Suppress("UNUSED")

package xyz.meowing.zen.managers.config

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.config.ui.ClickGUI
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.feature.FeatureManager
import xyz.meowing.zen.utils.Utils.toColorFromMap
import java.awt.Color

object ConfigManager {
    private val configFile = StoredFile("config/Config")
    private val categoryOrder = listOf("general", "qol", "hud", "visuals", "slayers", "dungeons", "meowing", "rift")
    private val pendingCallbacks = mutableListOf<Pair<String, (Any) -> Unit>>()

    val configValueMap: MutableMap<String, Any> = mutableMapOf()
    val configTree = mutableListOf<CategoryElement>()

    lateinit var configUI: ClickGUI

    private val jsonElementCodec: Codec<JsonElement> = Codec.PASSTHROUGH.xmap(
        { it.convert(JsonOps.INSTANCE).value },
        { Dynamic(JsonOps.INSTANCE, it) }
    )

    init {
        loadConfig()
    }

    private fun loadConfig() {
        configValueMap.clear()

        val categories by configFile.map(
            "categories",
            Codec.STRING,
            Codec.unboundedMap(
                Codec.STRING,
                Codec.unboundedMap(
                    Codec.STRING, jsonElementCodec
                )
            )
        )

        categories.forEach { (_, features) ->
            features.forEach { (_, options) ->
                options.forEach { (optionKey, jsonValue) ->
                    configValueMap[optionKey] = parseJsonElement(jsonValue)
                }
            }
        }
    }

    private fun parseJsonElement(element: JsonElement): Any {
        return when {
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> {
                        val num = primitive.asNumber
                        when {
                            num.toDouble() % 1.0 == 0.0 -> num.toInt()
                            else -> num.toDouble()
                        }
                    }
                    primitive.isString -> primitive.asString
                    else -> primitive.asString
                }
            }
            element.isJsonArray -> {
                element.asJsonArray.map { parseJsonElement(it) }
            }
            element.isJsonObject -> {
                element.asJsonObject.entrySet().associate { it.key to parseJsonElement(it.value) }
            }
            else -> element.toString()
        }
    }

    private fun toJsonElement(value: Any): JsonElement {
        return when (value) {
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is List<*> -> JsonArray().apply {
                value.forEach { add(toJsonElement(it ?: return@forEach)) }
            }
            is Map<*, *> -> JsonObject().apply {
                value.forEach { (k, v) ->
                    if (k is String && v != null) add(k, toJsonElement(v))
                }
            }
            is Color -> JsonObject().apply {
                addProperty("r", value.red)
                addProperty("g", value.green)
                addProperty("b", value.blue)
                addProperty("a", value.alpha)
            }
            else -> JsonPrimitive(value.toString())
        }
    }

    fun createConfigUI() {
        configUI = ClickGUI

        FeatureManager.features.forEach { it.addConfig() }
    }

    fun executePending() {
        pendingCallbacks.forEach { (configKey, callback) ->
            configUI.registerListener(configKey, callback)
        }

        pendingCallbacks.clear()
    }

    fun registerListener(configKey: String, instance: Any) {
        val callback: (Any) -> Unit = { _ ->
            if (instance is Feature) instance.update()
        }

        if (::configUI.isInitialized) configUI.registerListener(configKey, callback) else pendingCallbacks.add(configKey to callback)
    }

    fun addFeature(featureName: String, description: String, categoryName: String, element: ConfigElement): FeatureElement {
        val category = configTree.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
            ?: CategoryElement(categoryName).also { configTree.add(it) }

        configTree.sortWith(
            compareBy<CategoryElement> { cat ->
                categoryOrder.indexOf(cat.name.lowercase()).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }.thenBy { it.name }
        )

        val featureElement = FeatureElement(featureName, description, element)
        featureElement.configElement.parent = featureElement

        if (!category.features.any { it.featureName == featureName }) category.features.add(featureElement)

        ensureDefaultValue(element)

        return featureElement
    }

    fun ensureDefaultValue(element: ConfigElement) {
        if (configValueMap.containsKey(element.configKey)) return

        val defaultValue = when (val type = element.type) {
            is ElementType.Switch -> type.default
            is ElementType.Slider -> type.default
            is ElementType.Dropdown -> type.default
            is ElementType.TextInput -> type.default
            is ElementType.ColorPicker -> type.default
            is ElementType.Keybind -> type.default
            is ElementType.MultiCheckbox -> type.default.toList()
            is ElementType.MCColorPicker -> type.default.code
            else -> null
        }

        defaultValue?.let {
            configValueMap[element.configKey] = it
            saveConfig(false)
        }
    }

    fun saveConfig(force: Boolean) {
        val categories = mutableMapOf<String, MutableMap<String, MutableMap<String, JsonElement>>>()

        configTree.forEach { category ->
            val categoryMap = categories.getOrPut(category.name) { mutableMapOf() }

            category.features.forEach { feature ->
                val featureMap = categoryMap.getOrPut(feature.featureName) { mutableMapOf() }

                val featureKey = feature.configElement.configKey
                configValueMap[featureKey]?.let { value ->
                    val jsonValue = when (value) {
                        is Color -> toJsonElement(mapOf(
                            "r" to value.red,
                            "g" to value.green,
                            "b" to value.blue,
                            "a" to value.alpha
                        ))
                        else -> toJsonElement(value)
                    }
                    featureMap[featureKey] = jsonValue
                }

                feature.options.values.flatten().forEach { option ->
                    val optionKey = option.configElement.configKey
                    configValueMap[optionKey]?.let { value ->
                        val jsonValue = when (value) {
                            is Color -> toJsonElement(mapOf(
                                "r" to value.red,
                                "g" to value.green,
                                "b" to value.blue,
                                "a" to value.alpha
                            ))
                            else -> toJsonElement(value)
                        }
                        featureMap[optionKey] = jsonValue
                    }
                }
            }
        }

        @Suppress("VariableNeverRead")
        var categoriesProp by configFile.map(
            "categories",
            Codec.STRING,
            Codec.unboundedMap(
                Codec.STRING,
                Codec.unboundedMap(
                    Codec.STRING,
                    jsonElementCodec)
            )
        )

        @Suppress("AssignedValueIsNeverRead")
        categoriesProp = categories

        if (force) configFile.forceSave()
    }

    fun getConfigValue(configKey: String): Any? {
        return when (val value = configValueMap[configKey]) {
            is Map<*, *> -> value.toColorFromMap()
            is List<*> -> value.mapNotNull { (it as? Number)?.toInt() }.toSet()
            else -> value
        }
    }

    fun openConfig() {
        TickScheduler.Client.post {
            client.setScreen(configUI)
        }
    }
}
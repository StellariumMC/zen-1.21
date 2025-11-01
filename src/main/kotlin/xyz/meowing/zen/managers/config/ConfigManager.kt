@file:Suppress("UNUSED")

package xyz.meowing.zen.managers.config

import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.config.ui.ClickGUI
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.feature.FeatureManager
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.Utils.toColorFromMap

object ConfigManager {
    private val dataUtils = DataUtils("config", mutableMapOf<String, Any>())
    private val categoryOrder = listOf("general", "qol", "hud", "visuals", "slayers", "dungeons", "meowing", "rift")
    private val pendingCallbacks = mutableListOf<Pair<String, (Any) -> Unit>>()

    val configValueMap: MutableMap<String, Any> = dataUtils.getData()
    val configTree = mutableListOf<CategoryElement>()

    lateinit var configUI: ClickGUI

    fun createConfigUI() {
        configUI = ClickGUI()

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

        return featureElement
    }

    fun saveConfig(saveToFile: Boolean) {
        dataUtils.setData(configValueMap)
        if (saveToFile) dataUtils.save()
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
            client.execute { client.setScreen(configUI) }
        }
    }
}





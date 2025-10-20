package xyz.meowing.zen

import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import xyz.meowing.zen.config.ZenConfig
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.AreaEvent
import xyz.meowing.zen.events.GameEvent
import xyz.meowing.zen.events.GuiEvent
import xyz.meowing.zen.features.Debug
import xyz.meowing.zen.features.FeatureLoader
import xyz.meowing.zen.utils.LoopUtils
import xyz.meowing.zen.utils.NetworkUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import org.apache.logging.log4j.LogManager
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.loader.KnitModInfo
import xyz.meowing.knit.api.text.KnitText

data class firstInstall(val isFirstInstall: Boolean = true)

class Zen : ClientModInitializer {
    private var shown = false
    private lateinit var dataUtils: DataUtils<firstInstall>

    @Target(AnnotationTarget.CLASS)
    annotation class Module

    @Target(AnnotationTarget.CLASS)
    annotation class Command

    override fun onInitializeClient() {
        dataUtils = DataUtils("zen-data", firstInstall())
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (shown) return@register

            val loadMessage = KnitText
                .literal("$prefix §fMod loaded.")
                .onHover("§c${FeatureLoader.getFeatCount()} modules §8- §c${FeatureLoader.getLoadtime()}ms §8- §c${FeatureLoader.getCommandCount()} commands")
                .toVanilla()

            KnitChat.fakeMessage(loadMessage)

            val data = dataUtils.getData()

            if (data.isFirstInstall) {
                KnitChat.fakeMessage("$prefix §fThanks for installing Zen!")
                KnitChat.fakeMessage("§7> §fUse §c/zen §fto open the config or §c/zen hud §fto edit HUD elements")

                val discordMessage = KnitText
                    .literal("§7> §cDiscord:§b [Discord]")
                    .onHover("Discord server")
                    .onClick("https://discord.gg/KPmHQUC97G")
                    .toVanilla()

                KnitChat.fakeMessage(discordMessage)
                dataUtils.setData(data.copy(isFirstInstall = false))
                dataUtils.save()
            }
            if (Debug.debugmode) KnitChat.fakeMessage("$prefix §fYou have debug mode enabled, restart the game if this was not intentional.")

            LoopUtils.setTimeout(5000) {
                UpdateChecker.checkForUpdates()
            }

            shown = true
        }

        EventBus.register<GameEvent.Load> ({
            configUI = ZenConfig()
            FeatureLoader.init()
            initializeFeatures()
            executePending()
        })

        EventBus.register<GuiEvent.Open> { event ->
            if (event.screen is InventoryScreen) isInInventory = true
        }

        EventBus.register<GuiEvent.Close> {
            isInInventory = false
        }

        EventBus.register<AreaEvent.Main> ({
            TickUtils.scheduleServer(1) {
                areaFeatures.forEach { it.update() }
            }
        })

        EventBus.register<AreaEvent.Sub> ({
            TickUtils.scheduleServer(1) {
                subareaFeatures.forEach { it.update() }
            }
        })

        EventBus.register<AreaEvent.Skyblock> ({
            TickUtils.scheduleServer(1) {
                skyblockFeatures.forEach { it.update() }
            }
        })

        NetworkUtils.getJson(
            "https://api.hypixel.net/v2/resources/skyblock/election",
            onSuccess = { jsonObject ->
                if (jsonObject.get("success")?.asBoolean != true) return@getJson

                val dataElement = jsonObject.get("data") ?: return@getJson
                val dataObj = JsonParser().parse(dataElement.toString()).asJsonObject
                mayorData = com.google.gson.Gson().fromJson(dataObj, ApiMayor::class.java)
            },
            onError = { exception ->
                LOGGER.warn("Failed to fetch election data: ${exception.message}")
            }
        )
    }

    companion object {
        @JvmField var LOGGER = LogManager.getLogger("zen")
        private val pendingCallbacks = mutableListOf<Pair<String, (Any) -> Unit>>()
        private val pendingFeatures = mutableListOf<Feature>()
        private val areaFeatures = mutableListOf<Feature>()
        private val subareaFeatures = mutableListOf<Feature>()
        private val skyblockFeatures = mutableListOf<Feature>()
        lateinit var configUI: ConfigUI
        const val prefix = "§7[§bZen§7]"
        val features = mutableListOf<Feature>()
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        var isInInventory = false
        var mayorData: ApiMayor? = null

        // CHANGE THIS, I DIDNT KNOW HOW TO USE IT
        var modInfo = KnitModInfo("zen", "Zen", "1.1.8")

        private fun executePending() {
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

        fun addFeature(feature: Feature) = pendingFeatures.add(feature)

        fun initializeFeatures() {
            pendingFeatures.forEach { feature ->
                features.add(feature)
                if (feature.hasAreas()) areaFeatures.add(feature)
                if (feature.hasSubareas()) subareaFeatures.add(feature)
                if (feature.skyblockOnly) skyblockFeatures.add(feature)
                feature.addConfig()
                feature.initialize()
                feature.configKey?.let { registerListener(it, feature) }
                feature.update()
            }
            pendingFeatures.clear()
        }

        fun openConfig() {
            TickUtils.schedule(2) {
                client.execute {
                    if (::configUI.isInitialized) client.setScreen(configUI)
                }
            }
        }
    }
}


@Serializable
data class ApiMayor(@SerialName("mayor") val mayor: Candidate, ) {
    @Serializable
    data class Candidate(
        @SerialName("name")
        val name: String,
        @SerialName("perks")
        val perks: List<Perk> = emptyList(),
        @SerialName("minister")
        val minister: Candidate? = null
    )

    @Serializable
    data class Perk(val name: String, val description: String)
}
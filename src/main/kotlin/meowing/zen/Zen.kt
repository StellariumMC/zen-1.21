package meowing.zen

import meowing.zen.compat.OldConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import meowing.zen.config.ZenConfig
import meowing.zen.config.ui.ConfigUI
import meowing.zen.features.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.DataUtils
import meowing.zen.events.EventBus
import meowing.zen.events.AreaEvent
import meowing.zen.events.GameEvent
import meowing.zen.events.GuiEvent
import meowing.zen.features.Debug
import meowing.zen.features.FeatureLoader
import meowing.zen.utils.ChatUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.text.ClickEvent
import org.apache.logging.log4j.LogManager

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

            ChatUtils.addMessage(
                "$prefix §fMod loaded.",
                "§c${FeatureLoader.getFeatCount()} modules §8- §c${FeatureLoader.getLoadtime()}ms §8- §c${FeatureLoader.getCommandCount()} commands"
            )

            val data = dataUtils.getData()

            if (data.isFirstInstall) {
                ChatUtils.addMessage("$prefix §fThanks for installing Zen!")
                ChatUtils.addMessage("§7> §fUse §c/zen §fto open the config or §c/zen hud §fto edit HUD elements")
                ChatUtils.addMessage("§7> §cDiscord:§b [Discord]", "Discord server", ClickEvent.Action.OPEN_URL, "https://discord.gg/KPmHQUC97G")
                dataUtils.setData(data.copy(isFirstInstall = false))
                dataUtils.save()
            }
            if (Debug.debugmode) ChatUtils.addMessage("$prefix §fYou have debug mode enabled, restart the game if this was not intentional.")

            UpdateChecker.checkForUpdates()
            shown = true
        }

        EventBus.register<GameEvent.Load> ({
            OldConfig.convertConfig(FabricLoader.getInstance().configDir.toFile())
            configUI = ZenConfig()
            FeatureLoader.init()
            executePending()
        })

        EventBus.register<GuiEvent.Open> ({ event ->
            if (event.screen is InventoryScreen) isInInventory = true
        })

        EventBus.register<GuiEvent.Close> ({
            isInInventory = false
        })

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
    }

    companion object {
        @JvmField var LOGGER = LogManager.getLogger("zen")
        private val pendingCallbacks = mutableListOf<Pair<String, (Any) -> Unit>>()
        private val pendingFeatures = mutableListOf<Feature>()
        private val areaFeatures = mutableListOf<Feature>()
        private val subareaFeatures = mutableListOf<Feature>()
        lateinit var configUI: ConfigUI
        const val prefix = "§7[§bZen§7]"
        val features = mutableListOf<Feature>()
        val mc = MinecraftClient.getInstance()
        var isInInventory = false

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
                feature.addConfig(configUI)
                feature.initialize()
                feature.configKey?.let { registerListener(it, feature) }
                feature.update()
            }
            pendingFeatures.clear()
        }

        fun openConfig() {
            TickUtils.schedule(2) {
                mc.execute {
                    if (::configUI.isInitialized) mc.setScreen(configUI)
                }
            }
        }
    }
}
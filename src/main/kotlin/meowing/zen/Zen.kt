package meowing.zen

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import meowing.zen.config.ConfigAccessor
import meowing.zen.config.ZenConfig
import meowing.zen.config.ui.ConfigUI
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.DataUtils
import meowing.zen.events.EventBus
import meowing.zen.events.AreaEvent
import meowing.zen.events.EntityEvent
import meowing.zen.events.GameEvent
import meowing.zen.events.GuiEvent
import meowing.zen.events.RenderEvent
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.text.ClickEvent

data class firstInstall(val isFirstInstall: Boolean = true)

class Zen : ClientModInitializer {
    private var shown = false
    private lateinit var dataUtils: DataUtils<firstInstall>

    override fun onInitializeClient() {
        dataUtils = DataUtils("zen-data", firstInstall())

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (shown) return@register

            ChatUtils.addMessage("§c[Zen] §fMod loaded - §c${FeatureLoader.getFeatCount()} §ffeatures", "§c${FeatureLoader.getLoadtime()}ms §8- §c4 commands §7| §c8 utils")

            val data = dataUtils.getData()

            if (data.isFirstInstall) {
                ChatUtils.addMessage("§c[Zen] §fThanks for installing Zen!")
                ChatUtils.addMessage("§7> §fUse §c/zen §fto open the config or §c/zen hud §fto edit HUD elements")
                ChatUtils.addMessage("§7> §cDiscord:§b [Discord]", "Discord server", ClickEvent.Action.OPEN_URL, "https://discord.gg/KPmHQUC97G")
                dataUtils.setData(data.copy(isFirstInstall = false))
                dataUtils.save()
            }

            UpdateChecker.checkForUpdates()
            shown = true
        }

        EventBus.register<GameEvent.Load> ({
            configUI = ZenConfig()
            config = ConfigAccessor(configUI)
            FeatureLoader.init()
            executePendingCallbacks()
        })

        EventBus.register<GuiEvent.Open> ({ event ->
            if (event.screen is InventoryScreen) isInInventory = true
        })

        EventBus.register<GuiEvent.Close> ({
            isInInventory = false
        })

        EventBus.register<AreaEvent.Main> ({
            TickUtils.scheduleServer(1) {
                updateFeatures()
            }
        })
        EventBus.register<AreaEvent.Sub> ({
            TickUtils.scheduleServer(1) {
                updateFeatures()
            }
        })
    }

    companion object {
        val features = mutableListOf<Feature>()
        val mc = MinecraftClient.getInstance()
        var isInInventory = false
        lateinit var configUI: ConfigUI
        lateinit var config: ConfigAccessor
        private val pendingCallbacks = mutableListOf<Pair<String, (Any) -> Unit>>()

        private fun updateFeatures() {
            features.forEach { it.update() }
        }

        private fun executePendingCallbacks() {
            pendingCallbacks.forEach { (configKey, callback) ->
                configUI.registerListener(configKey, callback)
            }
            pendingCallbacks.clear()
        }

        fun registerListener(configKey: String, instance: Any) {
            if (::configUI.isInitialized) {
                configUI.registerListener(configKey) { newValue ->
                    val isEnabled = newValue as? Boolean ?: false
                    if (instance is Feature) instance.onToggle(isEnabled)
                }
            } else {
                pendingCallbacks.add(configKey to { newValue ->
                    val isEnabled = newValue as? Boolean ?: false
                    if (instance is Feature) instance.onToggle(isEnabled)
                })
            }
        }

        fun registerCallback(configKey: String, callback: (Any) -> Unit) {
            if (::configUI.isInitialized) configUI.registerListener(configKey, callback)
            else pendingCallbacks.add(configKey to callback)
        }

        fun addFeature(feature: Feature) {
            features.add(feature)
            feature.addConfig(configUI)
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
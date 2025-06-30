package meowing.zen

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import meowing.zen.config.ConfigAccessor
import meowing.zen.config.ZenConfig
import meowing.zen.config.ui.ConfigUI
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import com.mojang.brigadier.Command
import meowing.zen.events.EventBus
import meowing.zen.events.GuiCloseEvent
import meowing.zen.events.GuiOpenEvent
import meowing.zen.events.AreaEvent
import meowing.zen.events.GameLoadEvent
import meowing.zen.events.SubAreaEvent
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import meowing.zen.hud.HudEditorScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen

class Zen : ClientModInitializer {
    private var shown = false

    override fun onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val configCmd = Command<FabricClientCommandSource> { _ ->
                openConfig()
                1
            }

            val hudCmd = Command<FabricClientCommandSource> { _ ->
                TickUtils.schedule(2) {
                    val client = MinecraftClient.getInstance()
                    client.execute {
                        client.setScreen(HudEditorScreen())
                    }
                }
                1
            }

            dispatcher.register(
                ClientCommandManager.literal("zen")
                    .executes(configCmd)
                    .then(ClientCommandManager.literal("hud").executes(hudCmd))
            )

            dispatcher.register(ClientCommandManager.literal("ma").executes(configCmd))
            dispatcher.register(ClientCommandManager.literal("meowaddons").executes(configCmd))
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (shown) return@register
            ChatUtils.addMessage(
                "§c[Zen] §fMod loaded - §c${FeatureLoader.getFeatCount() + 1} §ffeatures",
                "§c${FeatureLoader.getLoadtime()}ms §8- §c4 commands §7| §c10 utils"
            )
            UpdateChecker.checkForUpdates()
            shown = true
        }

        EventBus.register<GameLoadEvent> ({
            configUI = ZenConfig()
            config = ConfigAccessor(configUI)
            FeatureLoader.init()
        })

        EventBus.register<GuiOpenEvent> ({ event ->
            if (event.screen is InventoryScreen) isInInventory = true
        })

        EventBus.register<GuiCloseEvent> ({
            isInInventory = false
        })

        EventBus.register<AreaEvent> ({ updateFeatures() })
        EventBus.register<SubAreaEvent> ({ updateFeatures() })
    }

    companion object {
        val features = mutableListOf<Feature>()
        val mc = MinecraftClient.getInstance()
        var isInInventory = false
        lateinit var configUI: ConfigUI
        lateinit var config: ConfigAccessor

        private fun updateFeatures() {
            features.forEach { it.update() }
        }

        fun registerListener(configKey: String, instance: Any) {
            configUI.registerListener(configKey) { newValue ->
                val isEnabled = newValue as? Boolean ?: false
                if (instance is Feature) {
                    instance.onToggle(isEnabled)
                }
            }
        }

        fun registerCallback(configKey: String, callback: (Any) -> Unit) {
            configUI.registerListener(configKey) { newValue ->
                callback(newValue)
            }
        }

        fun addFeature(feature: Feature) {
            features.add(feature)
        }

        fun openConfig() {
            TickUtils.schedule(2) {
                mc.execute {
                    mc.setScreen(configUI)
                }
            }
        }
    }
}
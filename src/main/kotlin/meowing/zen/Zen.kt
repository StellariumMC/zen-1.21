package meowing.zen

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import meowing.zen.config.ZenConfig
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import com.mojang.brigadier.Command
import meowing.zen.events.EventBus
import meowing.zen.events.GuiCloseEvent
import meowing.zen.events.GuiOpenEvent
import meowing.zen.events.AreaEvent
import meowing.zen.events.SubAreaEvent
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import meowing.zen.hud.HudEditorScreen
import java.util.concurrent.ConcurrentHashMap

class Zen : ClientModInitializer {
    private var shown = false

    override fun onInitializeClient() {
        ZenConfig.Handler.load()
        FeatureLoader.init()
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val configCmd = Command<FabricClientCommandSource> { _ ->
                TickUtils.schedule(2) {
                    val client = MinecraftClient.getInstance()
                    client.execute {
                        client.setScreen(ZenConfig.createConfigScreen(client.currentScreen))
                    }
                }
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
                "§c[Zen] §fMod loaded - §c${FeatureLoader.getFeatCount()} §ffeatures",
                "§c${FeatureLoader.getLoadtime()}ms §7| §c7 utils §7| §c4 commands"
            )
            UpdateChecker.checkForUpdates()
            shown = true
        }

        EventBus.register<GuiOpenEvent> ({ event ->
            if (event.screen is InventoryScreen) isInInventory = true
        })

        EventBus.register<GuiCloseEvent> ({ event ->
            if (event.screen is InventoryScreen) isInInventory = false
        })

        EventBus.register<AreaEvent> ({ updateFeatures() })
        EventBus.register<SubAreaEvent> ({ updateFeatures() })
    }

    companion object {
        private val features = mutableListOf<Feature>()
        private val configListeners = ConcurrentHashMap<String, MutableList<Feature>>()
        val mc = MinecraftClient.getInstance()
        val config: ZenConfig get() = ZenConfig.Handler.instance()
        var isInInventory = false

        fun addFeature(feature: Feature) {
            features.add(feature)
        }

        fun registerListener(configName: String, feature: Feature) {
            configListeners.getOrPut(configName) { mutableListOf() }.add(feature)
        }

        fun updateFeatures() {
            features.forEach { it.update() }
        }

        fun onConfigChange(configName: String) {
            configListeners[configName]?.forEach { it.update() }
        }
    }
}
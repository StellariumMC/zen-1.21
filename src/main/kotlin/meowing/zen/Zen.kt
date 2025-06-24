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
import meowing.zen.feats.FeatureLoader
import meowing.zen.utils.ChatUtils
import java.util.concurrent.ConcurrentHashMap

class Zen : ClientModInitializer {
    private var shown = false

    override fun onInitializeClient() {
        ZenConfig.Handler.load()
        FeatureLoader.init()
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val cmd = Command<FabricClientCommandSource> { _ ->
                TickUtils.schedule(2) {
                    val client = MinecraftClient.getInstance()
                    client.execute {
                        client.setScreen(ZenConfig.createConfigScreen(client.currentScreen))
                    }
                }
                1
            }
            dispatcher.register(ClientCommandManager.literal("zen").executes(cmd))
            dispatcher.register(ClientCommandManager.literal("ma").executes(cmd))
            dispatcher.register(ClientCommandManager.literal("meowaddons").executes(cmd))
        }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (shown) return@register
            ChatUtils.addMessage("§c[Zen] §fMod loaded - §c${FeatureLoader.getFeatCount()} §ffeatures", "§c${FeatureLoader.getLoadtime()}ms")
            shown = true
        }
    }

    companion object {
        private val features = mutableListOf<Feature>()
        private val configListeners = ConcurrentHashMap<String, MutableList<Feature>>()
        val mc = MinecraftClient.getInstance()
        val config: ZenConfig get() = ZenConfig.Handler.instance()

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
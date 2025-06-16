package meowing.zen

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import meowing.zen.utils.chatutils
import meowing.zen.config.zencfg
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.EventProxy
import com.mojang.brigadier.Command

object Zen : ClientModInitializer {
    private var shown = false

    override fun onInitializeClient() {
        val startTime = System.currentTimeMillis()
        zencfg.Handler.load()
        EventProxy.initialize()
        featManager.initAll()
        val loadTime = System.currentTimeMillis() - startTime

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val cmd = Command<FabricClientCommandSource> { _ ->
                TickScheduler.schedule(1) {
                    val client = MinecraftClient.getInstance()
                    client.execute {
                        client.setScreen(zencfg.createConfigScreen(client.currentScreen))
                        TickScheduler.schedule(2, featManager::onConfigChange)
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
            chatutils.clientmsg("§c[Zen] §fMod loaded in §c${loadTime}ms §7| §c${featManager.getModuleCount()} features", false)
            shown = true
        }
    }

    fun getConfig(): zencfg {
        return zencfg.Handler.instance()
    }
}
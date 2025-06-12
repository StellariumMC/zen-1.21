package meowing.zen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import meowing.zen.utils.chatutils;
import meowing.zen.config.zencfg;
import meowing.zen.utils.TickScheduler;
import meowing.zen.utils.EventProxy;
import com.mojang.brigadier.Command;

public class Zen implements ClientModInitializer {
    private static boolean shown = false;

    @Override
    public void onInitializeClient() {
        long startTime = System.currentTimeMillis();
        EventProxy.initialize();
        featManager.initAll();
        zencfg.Handler.load();
        long loadTime = System.currentTimeMillis() - startTime;
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            Command<FabricClientCommandSource> cmd = context -> {
                TickScheduler.schedule(1, () -> {
                    var client = MinecraftClient.getInstance();
                    client.execute(() -> {
                        client.setScreen(zencfg.createConfigScreen(client.currentScreen));
                        TickScheduler.schedule(2, featManager::onConfigChange);
                    });
                });
                return 1;
            };
            dispatcher.register(ClientCommandManager.literal("zen").executes(cmd));
            dispatcher.register(ClientCommandManager.literal("ma").executes(cmd));
            dispatcher.register(ClientCommandManager.literal("meowaddons").executes(cmd));
        });
        
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (shown) return;
            chatutils.clientmsg(String.format("§c[Zen] §fMod loaded in §c%dms §7| §c%d features", loadTime, featManager.moduleCount), false);
            shown = true;
        });
    }

    public static zencfg getConfig() {
        return zencfg.Handler.instance();
    }
}
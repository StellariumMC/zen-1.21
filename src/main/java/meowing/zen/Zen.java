package meowing.zen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import meowing.zen.config.zencfg;
import meowing.zen.utils.TickScheduler;

import java.util.Objects;

public class Zen implements ClientModInitializer {
    private static boolean shown = false;

	@Override
	public void onInitializeClient() {
        long startTime = System.currentTimeMillis();
		featLoader.initializeAll();
		zencfg.Handler.load();
		long loadTime = System.currentTimeMillis() - startTime;

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("zen")
						.executes(context -> {
							TickScheduler.register();
							TickScheduler.schedule(1, () -> {
								MinecraftClient client = MinecraftClient.getInstance();
								client.execute(() -> client.setScreen(zencfg.createConfigScreen(client.currentScreen)));
							});
							return 1;
						})
				)
		);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (shown) return;
			String message = String.format("§c[Zen] §fMod loaded in §c%dms §7| §c%d features", loadTime, featLoader.moduleCount);
			Objects.requireNonNull(client.player).sendMessage(Text.literal(message), false);
			shown = true;
		});
	}

	public static zencfg getConfig() {
		return zencfg.Handler.instance();
	}
}
package meowing.zen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import meowing.zen.feats.meowing.automeow;
import meowing.zen.config.zencfg;
import meowing.zen.utils.TickScheduler;

import java.util.Objects;

public class Zen implements ClientModInitializer {
	private static long startTime;
	private static int moduleCount = 0;
	private static boolean shown = false;

	@Override
	public void onInitializeClient() {
		startTime = System.currentTimeMillis();

		automeow.initialize();
		moduleCount++;
		zencfg.Handler.load();

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
			long loadTime = (System.currentTimeMillis() - startTime) / 1000;
			String message = String.format("§c[Zen] §fMod loaded in §c%ds §7| §c%d features", loadTime, moduleCount);
			Objects.requireNonNull(client.player).sendMessage(Text.literal(message), false);
			shown = true;
		});
	}

	public static zencfg getConfig() {
		return zencfg.Handler.instance();
	}
}
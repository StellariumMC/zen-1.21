package meowing.zen;

import net.fabricmc.api.ClientModInitializer;
import meowing.zen.feats.meowing.automeow;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import meowing.zen.config.zencfg;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Objects;

public class Zen implements ClientModInitializer {
	public static KeyBinding openConfigKey;
	private static long startTime;
	private static int moduleCount = 0;
	private static boolean shown = false;

	@Override
	public void onInitializeClient() {
		startTime = System.currentTimeMillis();

		automeow.initialize();
		moduleCount++;

		zencfg.Handler.load();

		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.zen.open_config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Z,
				"category.zen"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.wasPressed())
				MinecraftClient.getInstance().setScreen(zencfg.createConfigScreen(MinecraftClient.getInstance().currentScreen));
		});

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
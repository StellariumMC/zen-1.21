package meowing.zen;

import net.fabricmc.api.ClientModInitializer;
import meowing.zen.feats.meowing.automeow;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import meowing.zen.config.zencfg;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class Zen implements ClientModInitializer {
	public static KeyBinding openConfigKey;
	@Override
	public void onInitializeClient() {
		automeow.initialize();
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
	}
	public static zencfg getConfig() {
		return zencfg.Handler.instance();
	};
}
package meowing.zen

import net.fabricmc.api.ClientModInitializer
import meowing.zen.feats.meowing.automeow

object Zen : ClientModInitializer {
	override fun onInitializeClient() {
		automeow.initialize()
	}
}
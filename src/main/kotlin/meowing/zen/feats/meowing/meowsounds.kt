package meowing.zen.feats.meowing

import meowing.zen.utils.EventBus
import meowing.zen.utils.EventTypes
import meowing.zen.featManager
import meowing.zen.utils.utils
import net.minecraft.sound.SoundEvents

class meowsounds private constructor() {
    companion object {
        private val instance = meowsounds()

        @JvmStatic
        fun initialize() {
            featManager.register(instance) {
                EventBus.register(EventTypes.GameMessageEvent::class.java, instance, instance::onGameMessage)
            }
        }
    }

    private fun onGameMessage(event: EventTypes.GameMessageEvent) {
        val content = event.getPlainText().lowercase()
        if (content.contains("meow")) {
            utils.playSound(SoundEvents.ENTITY_CAT_AMBIENT, 0.8f, 1.0f)
        }
    }
}
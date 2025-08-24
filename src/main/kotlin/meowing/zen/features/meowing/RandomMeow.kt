package meowing.zen.features.meowing

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature
import meowing.zen.features.Timer
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TitleUtils
import meowing.zen.utils.Utils
import net.minecraft.sound.SoundEvents
import kotlin.random.Random

@Zen.Module
object RandomMeow : Feature("randommeow") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Random Meows", ConfigElement(
                "randommeow",
                null,
                ElementType.Switch(true)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        setupLoops {
            loopDynamic<Timer>({ Random.nextLong(3600000, 21600000) }) {
                if (Random.nextFloat() >= 0.01f){
                    ChatUtils.addMessage("$prefix §dmeow.")
                    Utils.playSound(SoundEvents.ENTITY_CAT_PURREOW, 1f, 1f)
                    TitleUtils.showTitle("§dmeow.", null, 2000)
                } else {
                    ChatUtils.addMessage("$prefix §dboo.")
                    Utils.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 1f, 1f)
                    TitleUtils.showTitle("§dBOOOOO.", null, 2000)
                }
            }
        }
    }
}
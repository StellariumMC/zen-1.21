package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.Timer
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TitleUtils
import xyz.meowing.zen.utils.Utils
import net.minecraft.sound.SoundEvents
import kotlin.random.Random

@Zen.Module
object RandomMeow : Feature("randommeow") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigManager
            .addFeature("Random Meows", "", "Meowing", xyz.meowing.zen.ui.ConfigElement(
                "randommeow",
                ElementType.Switch(true)
            ))
        return configUI
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
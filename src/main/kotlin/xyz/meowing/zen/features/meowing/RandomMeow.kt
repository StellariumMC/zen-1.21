package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.Timer
import xyz.meowing.zen.utils.TitleUtils
import xyz.meowing.zen.utils.Utils
import net.minecraft.sound.SoundEvents
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import kotlin.random.Random

@Module
object RandomMeow : Feature("randommeow") {
    override fun addConfig() {
        ConfigManager
            .addFeature("Random Meows", "", "Meowing", ConfigElement(
                "randommeow",
                ElementType.Switch(true)
            ))
    }


    override fun initialize() {
        setupLoops {
            loopDynamic<Timer>({ Random.nextLong(3600000, 21600000) }) {
                if (Random.nextFloat() >= 0.01f){
                    KnitChat.fakeMessage("$prefix §dmeow.")
                    Utils.playSound(SoundEvents.ENTITY_CAT_PURREOW, 1f, 1f)
                    TitleUtils.showTitle("§dmeow.", null, 2000)
                } else {
                    KnitChat.fakeMessage("$prefix §dboo.")
                    Utils.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 1f, 1f)
                    TitleUtils.showTitle("§dBOOOOO.", null, 2000)
                }
            }
        }
    }
}
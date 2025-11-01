package xyz.meowing.zen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.fabricmc.api.ClientModInitializer
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.features.Debug
import xyz.meowing.zen.utils.LoopUtils
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.loader.KnitModInfo
import xyz.meowing.knit.api.text.KnitText
import xyz.meowing.zen.events.core.GameEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.ServerEvent
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.feature.FeatureManager

data class FirstInstall(val isFirstInstall: Boolean = true)

object Zen : ClientModInitializer {
    private var showLoad = true
    private var dataUtils: DataUtils<FirstInstall> = DataUtils("zen-data", FirstInstall())

    @JvmStatic
    var isInInventory = false
        private set

    @JvmStatic
    val prefix = "§7[§bZen§7]"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger("zen")

    @JvmStatic
    val modInfo = KnitModInfo("zen", "Zen", "1.1.8")

    @JvmStatic
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onInitializeClient() {
        EventBus.post(GameEvent.ModInit.Pre())

        ConfigManager.createConfigUI()
        FeatureManager.loadFeatures()
        FeatureManager.initializeFeatures()
        ConfigManager.executePending()

        EventBus.register<ServerEvent.Connect> {
            if (!showLoad) return@register

            val loadMessage = KnitText
                .literal("$prefix §fMod loaded.")
                .onHover("§c${FeatureManager.moduleCount} modules §8- §c${FeatureManager.loadTime}ms §8- §c${FeatureManager.commandCount} commands")
                .toVanilla()

            KnitChat.fakeMessage(loadMessage)

            val data = dataUtils.getData()

            if (data.isFirstInstall) {
                KnitChat.fakeMessage("$prefix §fThanks for installing Zen!")
                KnitChat.fakeMessage("§7> §fUse §c/zen §fto open the config or §c/zen hud §fto edit HUD elements")

                val discordMessage = KnitText
                    .literal("§7> §cDiscord:§b [Discord]")
                    .onHover("Discord server")
                    .onClick("https://discord.gg/KPmHQUC97G")
                    .toVanilla()

                KnitChat.fakeMessage(discordMessage)
                dataUtils.setData(data.copy(isFirstInstall = false))
                dataUtils.save()
            }

            if (Debug.debugmode) KnitChat.fakeMessage("$prefix §fYou have debug mode enabled, restart the game if this was not intentional.")

            LoopUtils.setTimeout(5000) {
                UpdateChecker.checkForUpdates()
            }

            showLoad = false
        }

        EventBus.register<GuiEvent.Open> { event ->
            if (event.screen is InventoryScreen) isInInventory = true
        }

        EventBus.register<GuiEvent.Close> {
            isInInventory = false
        }

        EventBus.post(GameEvent.ModInit.Post())
    }
}
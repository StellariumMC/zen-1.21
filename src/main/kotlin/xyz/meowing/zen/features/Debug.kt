package xyz.meowing.zen.features

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.api.skyblock.EntityDetection.sbMobID
import xyz.meowing.zen.api.skyblock.PlayerStats
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonAPI
import xyz.meowing.zen.config.dsl.Config
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.feature.FeatureManager.features
import xyz.meowing.zen.updateChecker.StateTracker
import xyz.meowing.zen.updateChecker.UpdateChecker
import java.awt.Color

@Module
object Debug : Feature() {
    var debugMode: Boolean by Zen.saveData.boolean("debugMode", false)

    init {
        createCustomEvent<RenderEvent.Entity.Post>("mobid") { event ->
            Render3D.drawString(
                event.entity.sbMobID ?: return@createCustomEvent,
                event.entity.position(),
                depth = true
            )
        }

        if (debugMode) {
            registerEvent("mobid")

            val debug by Config("Config test", "Config Debug Stuff", "Debug")
            debug.switch("Switch")
            debug.button("Click me!") { Zen.LOGGER.info("Button clicked!") }
            debug.slider("Slider", 50.0, 0.0, 100.0, false)
            debug.slider("Slider double", 50.0, 0.0, 100.0, true)
            debug.dropdown("Dropdown", listOf("Option 1", "Option 2", "Option 3", "Option 4"))
            debug.multiCheckbox("Multi-checkbox", listOf("Feature A", "Feature B", "Feature C", "Feature D", "Feature E"))
            debug.textInput("Text input", "Default text", "Enter text here...")
            debug.textParagraph("This is a text paragraph element used for displaying information or instructions to the user. It can contain multiple lines of text.")
            debug.colorPicker("Colorpicker")
            debug.keybind("Keybind")
        }
    }
}

@Command
object DebugCommand : Commodore("zendebug", "zd") {
    init {
        executable {
            runs { action: String ->
                when (action.lowercase()) {
                    "toggle" -> {
                        Debug.debugMode = !Debug.debugMode

                        if (Debug.debugMode) Debug.registerEvent("mobid") else Debug.unregisterEvent("mobid")
                        KnitChat.fakeMessage("$prefix §fToggled dev mode. You will need to restart to see the difference in the Config UI")
                    }
                    "stats" -> {
                        KnitChat.fakeMessage(
                            "§cHealth: ${PlayerStats.health} | Max: ${PlayerStats.maxHealth} | §6Absorb: ${PlayerStats.absorption}\n" +
                                    "§9Mana: ${PlayerStats.mana} | Max: ${PlayerStats.maxMana} | §3Overflow: ${PlayerStats.overflowMana}\n" +
                                    "§dRift Time: ${PlayerStats.riftTimeSeconds} | Max: ${PlayerStats.maxRiftTime}\n" +
                                    "§aDefense: ${PlayerStats.defense} | Effective: ${PlayerStats.effectiveHealth} | Effective Max: ${PlayerStats.maxEffectiveHealth}"
                        )
                    }
                    "dgutils" -> {
                        KnitChat.fakeMessage(
                            "Crypt Count: ${DungeonAPI.cryptCount}\n" +
                                    "Current Class: ${DungeonAPI.dungeonClass?.displayName} ${DungeonAPI.classLevel}\n" +
                                    "Cata: ${DungeonAPI.cataLevel}"
                        )
                    }
                    "regfeats" -> {
                        KnitChat.fakeMessage("Features registered:")
                        features.forEach {
                            if (it.isEnabled()) KnitChat.fakeMessage("§f> §c${it.configKey}")
                        }
                    }
                    "forceupdate" -> {
                        StateTracker.forceUpdate = true
                        UpdateChecker.check()
                    }
                    else -> {
                        KnitChat.fakeMessage("$prefix §fUsage: §7/§bzendebug §c<toggle|stats|dgutils|regfeats|forceupdate>")
                    }
                }
            }
        }

        runs {
            KnitChat.fakeMessage("$prefix §fUsage: §7/§bzendebug §c<toggle|stats|dgutils|regfeats|forceupdate>")
        }
    }
}
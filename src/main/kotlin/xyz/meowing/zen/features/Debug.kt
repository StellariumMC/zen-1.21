package xyz.meowing.zen.features

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.prefix
import xyz.meowing.zen.api.skyblock.EntityDetection.sbMobID
import xyz.meowing.zen.api.skyblock.PlayerStats
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.annotations.Command
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonAPI
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
        }
    }

    override fun addConfig() {
        if (!debugMode) return

        ConfigManager
            .addFeature(
                "Config test",
                "",
                "Debug",
                ConfigElement(
                    "debug",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Switch",
                ConfigElement(
                    "debug.testSwitch",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Button",
                ConfigElement(
                    "debug.testButton",
                    ElementType.Button("Click Me!") {
                        Zen.LOGGER.info("Button clicked!")
                    }
                )
            )
            .addFeatureOption(
                "Slider",
                ConfigElement(
                    "debug.testSlider",
                    ElementType.Slider(0.0, 100.0, 50.0, false)
                )
            )
            .addFeatureOption(
                "Slider double",
                ConfigElement(
                    "debug.testSliderDouble",
                    ElementType.Slider(0.0, 10.0, 5.5, true)
                )
            )
            .addFeatureOption(
                "Dropdown",
                ConfigElement(
                    "debug.testDropdown",
                    ElementType.Dropdown(listOf("Option 1", "Option 2", "Option 3", "Option 4"), 0)
                )
            )
            .addFeatureOption(
                "Text input",
                ConfigElement(
                    "debug.testTextInput",
                    ElementType.TextInput("Default text", "Enter text here...", 50)
                )
            )
            .addFeatureOption(
                "Empty text input",
                ConfigElement(
                    "debug.testTextInputEmpty",
                    ElementType.TextInput("", "Type something...", 100)
                )
            )
            .addFeatureOption(
                "Text paragraph",
                ConfigElement(
                    "debug.testParagraph",
                    ElementType.TextParagraph("This is a text paragraph element used for displaying information or instructions to the user. It can contain multiple lines of text.")
                )
            )
            .addFeatureOption(
                "Color picker",
                ConfigElement(
                    "debug.testColorPicker",
                    ElementType.ColorPicker(Color(100, 200, 255))
                )
            )
            .addFeatureOption(
                "Keybind",
                ConfigElement(
                    "debug.testKeybind",
                    ElementType.Keybind(82)
                )
            )
            .addFeatureOption(
                "Multi checkbox",
                ConfigElement(
                    "debug.testMultiCheckbox",
                    ElementType.MultiCheckbox(
                        options = listOf("Feature A", "Feature B", "Feature C", "Feature D", "Feature E"),
                        default = setOf(0, 2)
                    )
                )
            )
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
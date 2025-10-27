package xyz.meowing.zen.config

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.openConfig
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.knit.api.command.Commodore
// import xyz.meowing.zen.Zen.Companion.configUI
import xyz.meowing.zen.Zen.Companion.prefix
import java.lang.Exception

@Zen.Command
object ConfigCommand : Commodore("zen", "ma", "meowaddons") {
    init {
        literal("hud") {
            runs {
                TickUtils.schedule(1) {
                    client.execute {
                        client.setScreen(HUDEditor())
                    }
                }
            }
        }

        literal("updateConfig") {
            runs { configName: String, newValue: String, silent: Boolean ->
                try {
                    val value = parseValue(newValue)
                    // configUI.updateConfig(configName, value)
                    if (!silent) KnitChat.fakeMessage("$prefix §fUpdated config §b$configName §fto §b$value§f.")
                    Zen.LOGGER.info("Updated config $configName to value $value [${value.javaClass}]")
                } catch (e: Exception) {
                    Zen.LOGGER.error("Caught exception in command \"/zen updateConfig\": $e")
                }
            }
        }

        runs {
            openConfig()
        }
    }

    private fun parseValue(input: String): Any {
        return when {
            input.toBooleanStrictOrNull() != null -> input.toBoolean()
            input.toIntOrNull() != null -> input.toInt()
            input.toDoubleOrNull() != null -> input.toDouble()
            input.toFloatOrNull() != null -> input.toFloat()
            else -> input
        }
    }
}

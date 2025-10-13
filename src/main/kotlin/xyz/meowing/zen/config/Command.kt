package xyz.meowing.zen.config

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.Zen.Companion.openConfig
import xyz.meowing.zen.hud.HUDEditor
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.knit.api.command.Commodore

@Zen.Command
object ConfigCommand : Commodore("zen", "ma", "meowaddons") {
    init {
        literal("hud") {
            runs {
                TickUtils.schedule(1) {
                    mc.execute {
                        mc.setScreen(HUDEditor())
                    }
                }
            }
        }

        runs {
            openConfig()
        }
    }
}
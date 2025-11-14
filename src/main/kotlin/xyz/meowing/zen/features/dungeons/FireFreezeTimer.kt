package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.sounds.SoundEvents
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonFloor
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object FireFreezeTimer : Feature(
    "fireFreezeTimer",
    island = SkyBlockIsland.THE_CATACOMBS,
    dungeonFloor = listOf(DungeonFloor.F3, DungeonFloor.M3)
) {
    private const val NAME = "Fire Freeze Timer"
    var ticks = 0

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Fire freeze timer",
                "Shows a timer for Fire Freeze staff ability in dungeons",
                "Dungeons",
                ConfigElement(
                    "fireFreezeTimer",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.register(NAME, "§bFire freeze: §c4.3s", "fireFreezeTimer")

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                createTimer(105,
                    onTick = {
                        if (ticks > 0) ticks--
                    },
                    onComplete = {
                        Utils.playSound(SoundEvents.ANVIL_LAND, 1f, 0.5f)
                        ticks = 0
                    }
                )
                ticks = 100
            }
        }

        register<GuiEvent.Render.HUD> { renderHUD(it.context) }

        register<LocationEvent.WorldChange> { ticks = 0 }
    }

    override fun onRegister() {
        ticks = 0
        super.onRegister()
    }

    override fun onUnregister() {
        ticks = 0
        super.onUnregister()
    }

    private fun renderHUD(context: GuiGraphics) {
        if (ticks <= 0) return

        val text = "§bFire freeze: §c${"%.1f".format(ticks / 20.0)}s"
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        Render2D.renderString(context, text, x, y, scale)
    }
}
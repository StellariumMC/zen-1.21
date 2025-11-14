package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.api.skyblock.PlayerStats
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Render2D.width
import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

/**
 * @author Eclipse-5214
 */
@Module
object RoomSecrets : Feature(
    "roomSecrets",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private const val NAME = "Secrets Display"

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Room secrets HUD",
                "Displays the number of secrets found in the current dungeon room",
                "Dungeons",
                ConfigElement(
                    "roomSecrets",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.registerCustom(NAME, 50, 30, this::editorRender, "roomSecrets")

        register<GuiEvent.Render.HUD> { renderHUD(it.context) }
    }

    fun editorRender(context: GuiGraphics) {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)

        val matrix = context.pose()
        //#if MC >= 1.21.7
        //$$ matrix.pushMatrix()
        //$$ matrix.translate(x, y)
        //#else
        matrix.pushPose()
        matrix.translate(x, y, 0f)
        //#endif

        val text1 = "§fSecrets"
        val text2 = "§a7§7/§a7"
        val w1 = text1.width().toFloat()
        val w2 = text2.width().toFloat()

        //#if MC >= 1.21.7
        //$$ matrix.translate(25f, 5f)
        //#else
        matrix.translate(25f, 5f, 0f)
        //#endif

        Render2D.renderString(context, text1, -w1 / 2f, 0f, 1f)
        Render2D.renderString(context, text2, -w2 / 2f, 10f, 1f)

        //#if MC >= 1.21.7
        //$$ matrix.popMatrix()
        //#else
        matrix.popPose()
        //#endif
    }

    private fun renderHUD(context: GuiGraphics) {
        val matrix = context.pose()
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)

        //#if MC >= 1.21.7
        //$$ matrix.pushMatrix()
        //$$ matrix.scale(scale, scale)
        //$$ matrix.translate(x, y)
        //#else
        matrix.pushPose()
        matrix.scale(scale, scale, 1f)
        matrix.translate(x, y, 0f)
        //#endif

        val text1 = "§fSecrets"
        val text2 = getText()
        val w1 = text1.width().toFloat()
        val w2 = text2.width().toFloat()

        //#if MC >= 1.21.7
        //$$ matrix.translate(25f, 5f)
        //#else
        matrix.translate(25f, 5f, 0f)
        //#endif

        Render2D.renderString(context, text1, -w1 / 2f, 0f, 1f)
        Render2D.renderString(context, text2, -w2 / 2f, 10f, 1f)

        //#if MC >= 1.21.7
        //$$ matrix.popMatrix()
        //#else
        matrix.popPose()
        //#endif
    }

    private fun getText(): String {
        val found = PlayerStats.currentRoomSecrets
        val total = PlayerStats.currentRoomMaxSecrets
        var text: String

        if ((found == 0 || found == -1) && total == 0) {
            text = "§7None"
            return text
        }

        val percent = found.toFloat() / total.toFloat()

        text = when {
            percent < 0.5f -> "§c$found§7/§c$total"
            percent <   1f -> "§e$found§7/§e$total"
            else           -> "§a$found§7/§a$total"
        }

        return text
    }
}
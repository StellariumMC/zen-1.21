package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.api.PlayerStats
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Render2D.width
import net.minecraft.client.gui.DrawContext
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

/**
 * @author Eclipse-5214
 */
@Module
object RoomSecrets : Feature("roomsecrets", island = SkyBlockIsland.THE_CATACOMBS) {
    private const val name = "Secrets Display"

    override fun addConfig() {
        ConfigManager
            .addFeature("Room Secrets Hud", "", "Dungeons", ConfigElement(
                "roomsecrets",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.registerCustom(name, 50, 30, this::HUDEditorRender)

        register<GuiEvent.Render.HUD> { renderHUD(it.context) }
    }

    fun HUDEditorRender(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean) {
        val matrix = context.matrices
        //#if MC >= 1.21.7
        //$$ matrix.pushMatrix()
        //$$ matrix.translate(x, y)
        //#else
        matrix.push()
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
        matrix.pop()
        //#endif
    }

    private fun renderHUD(context: DrawContext) {
        if (!HUDManager.isEnabled(name)) return
        val matrix = context.matrices
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        //#if MC >= 1.21.7
        //$$ matrix.pushMatrix()
        //$$ matrix.scale(scale, scale)
        //$$ matrix.translate(x, y)
        //#else
        matrix.push()
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
        matrix.pop()
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
package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.api.PlayerStats
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Render2D.width
import net.minecraft.client.gui.DrawContext

@Zen.Module
/**
 * @author Eclipse-5214
 */
object RoomSecrets : Feature("roomsecrets", area = "catacombs") {
    private const val name = "Secrets Display"

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Room Secrets Hud", ConfigElement(
                "roomsecrets",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.registerCustom(name, 50, 30, this::HUDEditorRender)

        register<RenderEvent.HUD> { renderHUD(it.context) }
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
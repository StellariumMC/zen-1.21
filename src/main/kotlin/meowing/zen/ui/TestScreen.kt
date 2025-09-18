package meowing.zen.ui

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.gui.Pos
import meowing.zen.utils.gui.Size
import meowing.zen.utils.gui.components.NanoRectangle
import meowing.zen.utils.gui.components.NanoText
import meowing.zen.utils.rendering.NVGRenderer
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Color

class NewConfigScreen : Screen(Text.literal("Test")) {
    private val mainBoundingRectangle = NanoRectangle().apply {
        backgroundColor = Color(40, 40, 40, 200).rgb
        borderColor = Color.CYAN.rgb
        borderThickness = 3f
        borderRadius = 10f
        setSizing(70f, Size.ScreenPerc, 70f, Size.ScreenPerc)
        setPositioning(Pos.ScreenCenter, Pos.ScreenCenter)
    }

    private val logo = NanoText("Zen").apply {
        textColor = Color.CYAN.rgb
        fontSize = 50f
        setPositioning(20f, Pos.ParentPixels, 20f, Pos.ParentPixels)
        childOf(mainBoundingRectangle)
    }

    override fun render(drawContext: DrawContext?, i: Int, j: Int, f: Float) {
        NVGRenderer.beginFrame(mc.window.width.toFloat(), mc.window.height.toFloat())
        NVGRenderer.push()
        mainBoundingRectangle.render(Utils.MouseX.toFloat(), Utils.MouseY.toFloat())
        NVGRenderer.pop()
        NVGRenderer.endFrame()
    }

    override fun shouldPause(): Boolean {
        return false
    }
}

@Zen.Command
object TestCommand : CommandUtils(
    "testscreen"
) {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        TickUtils.schedule(2) {
            mc.setScreen(NewConfigScreen())
        }
        return 1
    }
}
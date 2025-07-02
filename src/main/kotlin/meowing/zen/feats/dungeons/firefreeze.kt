package meowing.zen.feats.dungeons

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.EventBus
import meowing.zen.events.TickEvent
import meowing.zen.hud.HudElement
import meowing.zen.hud.HudManager
import meowing.zen.hud.HudRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Colors

object firefreeze : Feature("firefreeze") {
    var ticks = 0
    private var servertickcall: EventBus.EventCall = EventBus.register<TickEvent.Server>({
        if (ticks > 0) ticks--
    }, false)
    private var hudElement: HudElement? = null

    override fun initialize() {
        hudElement = HudElement(10f, 10f, 150f, 20f, 1.0f, true, "firefreeze", "Fire Freeze")
        HudManager.registerCustom(hudElement!!, FireFreezeRenderer(hudElement!!))

        register<ChatEvent.Receive> { event ->
            if (event.message!!.string.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                ticks = 100
                servertickcall.register()
                TickUtils.scheduleServer(105) {
                    Utils.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1f, 0.5f)
                    ticks = 0
                    servertickcall.unregister()
                }
            }
        }
    }

    override fun onRegister() {
        ticks = 0
    }

    override fun onUnregister() {
        ticks = 0
        servertickcall.unregister()
    }
}

class FireFreezeRenderer(element: HudElement) : HudRenderer(element) {
    override fun render(context: DrawContext, tickCounter: RenderTickCounter) {
        val text = getText()
        if (text.isEmpty() && !HudManager.editMode) return
        val displayText = if (HudManager.editMode && text.isEmpty()) "§bFire freeze: §c4.3s" else text
        if (displayText.isNotEmpty()) {
            val actualX = element.getActualX(mc.window.scaledWidth)
            val actualY = element.getActualY(mc.window.scaledHeight)

            context.matrices.push()
            context.matrices.translate(actualX.toDouble(), actualY.toDouble(), 0.0)
            context.matrices.scale(element.scale, element.scale, 1.0f)
            context.drawText(mc.textRenderer, displayText, 0, 0, Colors.WHITE, false)
            context.matrices.pop()
        }
    }

    override fun getPreviewSize(): Pair<Float, Float> {
        val text = if (firefreeze.ticks > 0) getText() else "§bFire freeze: §c4.3s"
        return Pair(
            mc.textRenderer.getWidth(text) * element.scale,
            mc.textRenderer.fontHeight * element.scale
        )
    }

    private fun getText(): String {
        if (firefreeze.ticks > 0) return "§bFire freeze: §c${"%.1f".format(firefreeze.ticks / 20.0)}s"
        return ""
    }
}
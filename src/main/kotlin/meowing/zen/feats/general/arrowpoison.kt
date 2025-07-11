package meowing.zen.feats.general

import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GuiEvent
import meowing.zen.events.PacketEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket

object arrowpoison : Feature("arrowpoison") {
    private const val name = "ArrowPoison"
    private var twilight = 0
    private var toxic = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Arrow poison tracker", ConfigElement(
                "arrowpoison",
                "Arrow poison tracker",
                "Tracks the arrow poisons inside your inventory.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register(name, "<I> 64 | <I> 32")

        register<PacketEvent.Received> { event ->
            ChatUtils.addMessage("packet received: ${event.packet.packetType} | ${event.packet.packetType.id}")
            if (event.packet is InventoryS2CPacket || event.packet is SetPlayerInventoryS2CPacket || event.packet is ScreenHandlerSlotUpdateS2CPacket) updateCount()
        }

        register<GuiEvent.HUD> { render(it.context) }
    }

    private fun updateCount() {
        twilight = 0
        toxic = 0
        if (mc.player == null || mc?.player?.inventory == null) return
        mc.player!!.inventory.mainStacks.forEach { item ->
            if (item == null) return@forEach
            val name = item.name.string.removeFormatting()
            if (name.contains("Twilight Arrow Poison")) twilight += item.count
            if (name.contains("Toxic Arrow Poison")) toxic += item.count
        }
    }

    private fun render(drawContext: DrawContext) {
        if (twilight == 0 && toxic == 0) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        drawHUD(drawContext, x, y, scale)
    }

    private fun drawHUD(drawContext: DrawContext, x: Float, y: Float, scale: Float) {
        val iconSize = 16f * scale
        val spacing = 4f * scale
        val twilightPotion = ItemStack(Items.PURPLE_DYE)
        val toxicPotion = ItemStack(Items.LIME_DYE)
        val fontRenderer = mc.textRenderer

        val matrixStack = drawContext.matrices
        matrixStack.push()
        matrixStack.scale(scale, scale, 1f)
        val scaledX = (x / scale).toInt()
        val scaledY = (y / scale).toInt()

        drawContext.drawItem(twilightPotion, scaledX, scaledY)
        matrixStack.pop()

        val twilightStr = twilight.toString()
        val textY = y + (iconSize - 8f * scale) / 2f
        val twilightTextX = x + iconSize + spacing
        Render2D.renderStringWithShadow(drawContext, twilightStr, twilightTextX, textY, scale)

        val separatorX = twilightTextX + fontRenderer.getWidth(twilightStr) * scale + spacing * 2
        Render2D.renderStringWithShadow(drawContext, "§7|", separatorX, textY, scale)

        val toxicIconX = separatorX + fontRenderer.getWidth("|") * scale + spacing
        matrixStack.push()
        matrixStack.scale(scale, scale, 1f)
        drawContext.drawItem(toxicPotion, (toxicIconX / scale).toInt(), scaledY)
        matrixStack.pop()

        val toxicTextX = toxicIconX + iconSize + spacing
        Render2D.renderStringWithShadow(drawContext, toxic.toString(), toxicTextX, textY, scale)
    }
}
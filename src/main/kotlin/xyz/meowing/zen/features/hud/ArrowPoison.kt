package xyz.meowing.zen.features.hud

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.PacketEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket

@Zen.Module
object ArrowPoison : Feature("arrowpoison", true) {
    private const val name = "ArrowPoison"
    private var twilight = 0
    private var toxic = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigManager
            .addFeature("Arrow poison tracker", "", "HUD", xyz.meowing.zen.ui.ConfigElement(
                "arrowpoison",
                ElementType.Switch(false)
            ))
        return configUI
    }


    override fun initialize() {
        HUDManager.registerCustom(name, 85, 17, this::HUDEditorRender)

        register<PacketEvent.Received> { event ->
            if (event.packet is InventoryS2CPacket || event.packet is SetPlayerInventoryS2CPacket || event.packet is ScreenHandlerSlotUpdateS2CPacket) updateCount()
        }

        register<RenderEvent.HUD> { event ->
            if (HUDManager.isEnabled(name)) render(event.context)
        }
    }

    private fun updateCount() {
        twilight = 0
        toxic = 0
        val inventory = player?.inventory?.mainStacks ?: return
        inventory.forEach { item ->
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
        drawHUD(drawContext, x, y, scale, false)
    }

    @Suppress("UNUSED")
    private fun HUDEditorRender(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean) {
        drawHUD(context, x, y, 1f, true)
    }

    private fun drawHUD(drawContext: DrawContext, x: Float, y: Float, scale: Float, preview: Boolean) {
        val iconSize = 16f * scale
        val spacing = 4f * scale
        val twilightPotion = ItemStack(Items.PURPLE_DYE)
        val toxicPotion = ItemStack(Items.LIME_DYE)
        val twilightStr = if (preview) "128" else twilight.toString()
        val toxicStr = if (preview) "92" else toxic.toString()
        val textY = y + (iconSize - 8f) / 2f
        var currentX = x

        Render2D.renderItem(drawContext, twilightPotion, currentX, y, scale)
        currentX += iconSize + spacing
        Render2D.renderStringWithShadow(drawContext, twilightStr, currentX, textY, scale)

        currentX += fontRenderer.getWidth(twilightStr) * scale + spacing * 2
        Render2D.renderStringWithShadow(drawContext, "§7|", currentX, textY, scale)

        currentX += fontRenderer.getWidth("|") * scale + spacing
        Render2D.renderItem(drawContext, toxicPotion, currentX, y, scale)

        currentX += iconSize + spacing
        Render2D.renderStringWithShadow(drawContext, toxicStr, currentX, textY, scale)
    }
}
package xyz.meowing.zen.features.hud

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.PacketEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object ArrowPoison : Feature(
    "arrowPoison",
    true
) {
    private const val NAME = "Arrow Poison"
    private var twilightCount = 0
    private var toxicCount = 0

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Arrow poison tracker",
                "Track arrow poisons present in your inventory",
                "HUD",
                ConfigElement(
                    "arrowPoison",
                    ElementType.Switch(false)
                )
            )
    }


    override fun initialize() {
        HUDManager.registerCustom(NAME, 85, 17, this::editorRender, "arrowPoison")

        register<PacketEvent.Received> { event ->
            if (event.packet is ClientboundContainerSetContentPacket || event.packet is ClientboundSetPlayerInventoryPacket || event.packet is ClientboundContainerSetSlotPacket) updateCount()
        }

        register<GuiEvent.Render.HUD> { event ->
            render(event.context)
        }
    }

    private fun updateCount() {
        twilightCount = 0
        toxicCount = 0
        val inventory = player?.inventory?.nonEquipmentItems ?: return
        inventory.forEach { item ->
            if (item == null) return@forEach
            val name = item.hoverName.string.removeFormatting()
            if (name.contains("Twilight Arrow Poison")) twilightCount += item.count
            if (name.contains("Toxic Arrow Poison")) toxicCount += item.count
        }
    }

    private fun render(drawContext: GuiGraphics) {
        if (twilightCount == 0 && toxicCount == 0) return
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)
        drawHUD(drawContext, x, y, scale, false)
    }

    private fun editorRender(context: GuiGraphics) {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        drawHUD(context, x, y, 1f, true)
    }

    private fun drawHUD(drawContext: GuiGraphics, x: Float, y: Float, scale: Float, preview: Boolean) {
        val iconSize = 16f * scale
        val spacing = 4f * scale
        val twilightPotion = ItemStack(Items.PURPLE_DYE)
        val toxicPotion = ItemStack(Items.LIME_DYE)
        val twilightStr = if (preview) "128" else twilightCount.toString()
        val toxicStr = if (preview) "92" else toxicCount.toString()
        val textY = y + (iconSize - 8f) / 2f
        var currentX = x

        Render2D.renderItem(drawContext, twilightPotion, currentX, y, scale)
        currentX += iconSize + spacing
        Render2D.renderStringWithShadow(drawContext, twilightStr, currentX, textY, scale)

        currentX += client.font.width(twilightStr) * scale + spacing * 2
        Render2D.renderStringWithShadow(drawContext, "§7|", currentX, textY, scale)

        currentX += client.font.width("|") * scale + spacing
        Render2D.renderItem(drawContext, toxicPotion, currentX, y, scale)

        currentX += iconSize + spacing
        Render2D.renderStringWithShadow(drawContext, toxicStr, currentX, textY, scale)
    }
}
package xyz.meowing.zen.features.hud

import xyz.meowing.zen.features.ClientTick
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import xyz.meowing.knit.api.KnitPlayer
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.GuiEvent

@Module
object ArmorHUD : Feature(
    "armorHud",
    "Armor HUD",
    "Display armor pieces on HUD",
    "HUD",
) {
    private const val NAME = "Armor HUD"
    private var armor = emptyList<ItemStack?>()

    private val exampleArmor = listOf(
        ItemStack(Items.DIAMOND_HELMET),
        ItemStack(Items.DIAMOND_CHESTPLATE),
        ItemStack(Items.DIAMOND_LEGGINGS),
        ItemStack(Items.DIAMOND_BOOTS)
    )

    private val vertical by config.switch("Render vertically", false)
    private val armorPieces by config.multiCheckbox("Pices to render", listOf("Helmet", "Chestplate", "Leggings", "Boots"), setOf(0, 1, 2, 3))

    override fun initialize() {
        HUDManager.registerCustom(
            NAME,
            if (vertical) 16 else 70,
            if (vertical) 70 else 16,
            this::editorRender,
            "armorHud"
        )

        setupLoops {
            loop<ClientTick>(20) {
                armor = KnitPlayer.armor.toList()
            }
        }

        register<GuiEvent.Render.HUD.Pre> { event ->
            render(event.context)
        }
    }

    private fun editorRender(context: GuiGraphics) = drawHUD(context, 0f, 0f, 1f, true)

    private fun render(context: GuiGraphics) {
        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)
        drawHUD(context, x, y, scale, false)
    }

    private fun drawHUD(context: GuiGraphics, x: Float, y: Float, scale: Float, preview: Boolean) {
        val iconSize = 16f * scale
        val spacing = 2f * scale
        val armorToRender = if (preview) exampleArmor else armor
        val selectedPieces = armorPieces

        var currentX = x
        var currentY = y

        armorToRender.reversed().forEachIndexed { index, item ->
            if (selectedPieces.contains(index)) {
                if (item != null) Render2D.renderItem(context, item, currentX, currentY, scale)
                if (vertical) currentY += iconSize + spacing
                else currentX += iconSize + spacing
            }
        }
    }
}
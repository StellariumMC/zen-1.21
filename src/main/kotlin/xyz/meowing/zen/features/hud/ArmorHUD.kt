package xyz.meowing.zen.features.hud

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.ClientTick
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

@Zen.Module
object ArmorHUD : Feature("armorhud") {
    private const val name = "Armor HUD"
    private var armor = emptyList<ItemStack>()
    private val armorhudvert by ConfigDelegate<Boolean>("armorhudvert")
    private val armorpieces by ConfigDelegate<Set<Int>>("armorpieces")

    private val exampleArmor = listOf(
        ItemStack(Items.DIAMOND_HELMET),
        ItemStack(Items.DIAMOND_CHESTPLATE),
        ItemStack(Items.DIAMOND_LEGGINGS),
        ItemStack(Items.DIAMOND_BOOTS)
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("HUD", "Armor HUD", ConfigElement(
                "armorhud",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("HUD", "Armor HUD", "Options", ConfigElement(
                "armorhudvert",
                "Vertical Armor HUD",
                ElementType.Switch(false)
            ))
            .addElement("HUD", "Armor HUD", "Options", ConfigElement(
                "armorpieces",
                "Armor pieces to render",
                ElementType.MultiCheckbox(
                    listOf("Helmet", "Chestplate", "Leggings", "Boots"),
                    setOf(0, 1, 2, 3)
                )
            ))
    }

    override fun initialize() {
        HUDManager.registerCustom(name, if (armorhudvert) 16 else 70, if (armorhudvert) 70 else 16, this::HUDEditorRender)

        setupLoops {
            loop<ClientTick>(20) {
                val player = player ?: return@loop
                armor = (0..3).map { slot ->
                    player.inventory.getStack(36 + slot)
                }.reversed()
            }
        }

        register<RenderEvent.HUD> { event ->
            if (HUDManager.isEnabled(name)) render(event.context)
        }
    }

    private fun render(context: DrawContext) {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        drawHUD(context, x, y, scale, false)
    }

    @Suppress("UNUSED")
    private fun HUDEditorRender(context: DrawContext, x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean) {
        drawHUD(context, x, y, 1f, true)
    }

    private fun drawHUD(context: DrawContext, x: Float, y: Float, scale: Float, preview: Boolean) {
        val iconSize = 16f * scale
        val spacing = 2f * scale
        val armorToRender = if (preview) exampleArmor else armor
        val selectedPieces = armorpieces

        var currentX = x
        var currentY = y

        armorToRender.forEachIndexed { index, item ->
            if (selectedPieces.contains(index)) {
                @Suppress("SENSELESS_COMPARISON")
                if (item != null) Render2D.renderItem(context, item, currentX, currentY, scale)
                if (armorhudvert) currentY += iconSize + spacing
                else currentX += iconSize + spacing
            }
        }
    }
}
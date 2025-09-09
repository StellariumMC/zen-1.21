package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Render3D
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.utils.Utils.toColorInt
import meowing.zen.utils.Utils.toFloatArray
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import java.awt.Color

@Zen.Module
object HighlightLivid : Feature("highlightlivid", area = "catacombs", subarea = listOf("F5", "M5")) {
    private var lividEntity: Entity? = null
    private val lividPos = BlockPos(5, 108, 42)
    private val lividTypes = mapOf(
        DyeColor.WHITE to "Vendetta",
        DyeColor.MAGENTA to "Crossed",
        DyeColor.PINK to "Crossed",
        DyeColor.RED to "Hockey",
        DyeColor.GRAY to "Doctor",
        DyeColor.GREEN to "Frog",
        DyeColor.LIME to "Smile",
        DyeColor.BLUE to "Scream",
        DyeColor.PURPLE to "Purple",
        DyeColor.YELLOW to "Arcade"
    )
    val stainedGlassBlocks = mapOf(
        Blocks.RED_STAINED_GLASS to DyeColor.RED,
        Blocks.ORANGE_STAINED_GLASS to DyeColor.ORANGE,
        Blocks.YELLOW_STAINED_GLASS to DyeColor.YELLOW,
        Blocks.LIME_STAINED_GLASS to DyeColor.LIME,
        Blocks.GREEN_STAINED_GLASS to DyeColor.GREEN,
        Blocks.CYAN_STAINED_GLASS to DyeColor.CYAN,
        Blocks.LIGHT_BLUE_STAINED_GLASS to DyeColor.LIGHT_BLUE,
        Blocks.BLUE_STAINED_GLASS to DyeColor.BLUE,
        Blocks.PURPLE_STAINED_GLASS to DyeColor.PURPLE,
        Blocks.MAGENTA_STAINED_GLASS to DyeColor.MAGENTA,
        Blocks.PINK_STAINED_GLASS to DyeColor.PINK,
        Blocks.WHITE_STAINED_GLASS to DyeColor.WHITE,
        Blocks.LIGHT_GRAY_STAINED_GLASS to DyeColor.LIGHT_GRAY,
        Blocks.GRAY_STAINED_GLASS to DyeColor.GRAY,
        Blocks.BLACK_STAINED_GLASS to DyeColor.BLACK,
        Blocks.BROWN_STAINED_GLASS to DyeColor.BROWN
    )
    private val highlightlividline by ConfigDelegate<Boolean>("highlightlividline")
    private val hidewronglivid by ConfigDelegate<Boolean>("hidewronglivid")
    private val highlightlividcolor by ConfigDelegate<Color>("highlightlividcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Highlight Livid", ConfigElement(
                "highlightlivid",
                "Highlight correct livid",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Highlight Livid", "Color", ConfigElement(
                "highlightlividcolor",
                "Highlight correct livid color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Dungeons", "Highlight Livid", "Options", ConfigElement(
                "hidewronglivid",
                "Hide incorrect livid entity",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Highlight Livid", "Options", ConfigElement(
                "highlightlividline",
                "Line to correct livid entity",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        createCustomEvent<RenderEvent.EntityGlow>("renderLivid") { event ->
            if (lividEntity == event.entity) {
                event.shouldGlow = true
                event.glowColor = highlightlividcolor.toColorInt()
            }
        }

        createCustomEvent<RenderEvent.World>("renderLine") { event ->
            lividEntity?.let { entity ->
                if (player?.canSee(entity) == true && event.context != null) {
                    Render3D.drawLineToEntity(
                        entity,
                        event.context,
                        highlightlividcolor.toFloatArray(),
                        highlightlividcolor.alpha.toFloat()
                    )
                }
            }
        }

        createCustomEvent<RenderEvent.Player.Pre>("renderWrong") { event ->
            if (event.entity != lividEntity && event.entity.name.contains(" Livid")) {
                event.cancel()
            }
        }

        createCustomEvent<TickEvent.Server>("tick") {
            val world = world ?: return@createCustomEvent
            val state: BlockState = mc.world?.getBlockState(lividPos) ?: return@createCustomEvent
            val color = stainedGlassBlocks[state.block] ?: return@createCustomEvent
            val lividType = lividTypes[color] ?: return@createCustomEvent

            world.players.find { it.name.contains(Text.literal(lividType)) }?.let {
                lividEntity = it
                registerRender()
                unregisterEvent("tick")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.") {
                TickUtils.scheduleServer(80) {
                    registerEvent("tick")
                }
            }
        }

        register<WorldEvent.Change> {
            unregisterRender()
        }
    }

    private fun registerRender() {
        registerEvent("renderLivid")
        if (hidewronglivid) registerEvent("renderWrong")
        if (highlightlividline) registerEvent("renderLine")
    }

    private fun unregisterRender() {
        unregisterEvent("renderLivid")
        unregisterEvent("renderWrong")
        unregisterEvent("renderLine")
        lividEntity = null
    }
}

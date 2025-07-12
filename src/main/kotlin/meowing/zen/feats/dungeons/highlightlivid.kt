package meowing.zen.feats.dungeons

import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils
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

object highlightlivid : Feature("highlightlivid", area = "catacombs", subarea = listOf("F5", "M5")) {
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

    private val renderLividCall: EventBus.EventCall = EventBus.register<RenderEvent.EntityGlow> ({ event ->
        if (lividEntity == event.entity) {
            event.shouldGlow = true
            event.glowColor = config.highlightlividcolor.toColorInt()
        }
    }, false)

    private val renderLineCall: EventBus.EventCall = EventBus.register<RenderEvent.World> ({ event ->
        lividEntity?.let { entity ->
            if (mc.player?.canSee(entity) == true) {
                RenderUtils.drawLineToEntity(
                    entity,
                    event.context!!,
                    config.highlightlividcolor.toFloatArray(),
                    config.highlightlividcolor.alpha.toFloat()
                )
            }
        }
    }, false)

    private val renderWrongCall: EventBus.EventCall = EventBus.register<RenderEvent.PlayerPre> ({ event ->
        if (event.entity != lividEntity && event.entity.name.removeFormatting().contains(" Livid"))
            event.cancel()
    }, false)

    private val tickCall: EventBus.EventCall = EventBus.register<TickEvent.Server>({
        val state: BlockState = mc.world?.getBlockState(lividPos) ?: return@register
        val color = stainedGlassBlocks[state.block] ?: return@register
        val lividType = lividTypes[color] ?: return@register

        mc.world!!.players.find { it.name.contains(Text.literal(lividType)) }?.let {
            lividEntity = it
            registerRender()
            tickCall.unregister()
        }
    }, false)

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Livid", ConfigElement(
                "highlightlivid",
                "Highlight correct livid",
                "Highlights the correct livid.",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Livid", ConfigElement(
                "highlightlividcolor",
                "Highlight correct livid color",
                "Color for the correct livid's outline",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["highlightlivid"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Livid", ConfigElement(
                "hidewronglivid",
                "Hide incorrect livid entity",
                "Cancels the rendering of incorrect livid entities",
                ElementType.Switch(false),
                { config -> config["highlightlivid"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Livid", ConfigElement(
                "highlightlividline",
                "Line to correct livid entity",
                "Renders a line to the correct livid entity",
                ElementType.Switch(false),
                { config -> config["highlightlivid"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.") {
                TickUtils.scheduleServer(80) {
                    tickCall.register()
                }
            }
        }

        register<WorldEvent.Change> {
            unregisterRender()
        }
    }

    private fun registerRender() {
        renderLividCall.register()
        if (config.hidewronglivid) renderWrongCall.register()
        if (config.highlightlividline) renderLineCall.register()
    }

    private fun unregisterRender() {
        renderLividCall.unregister()
        renderWrongCall.unregister()
        renderLineCall.unregister()
        lividEntity = null
    }
}

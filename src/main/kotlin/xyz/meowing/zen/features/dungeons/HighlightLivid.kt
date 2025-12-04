package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Render3D
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.utils.Utils.toFloatArray
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.Entity
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.core.BlockPos
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonFloor
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.events.core.TickEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.glowThisFrame
import xyz.meowing.zen.utils.glowingColor
import java.awt.Color

@Module
object HighlightLivid : Feature(
    "highlightLivid",
    island = SkyBlockIsland.THE_CATACOMBS,
    dungeonFloor = listOf(DungeonFloor.F5, DungeonFloor.M5)
) {
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

    private val highlightLividColor by ConfigDelegate<Color>("highlightLivid.color")
    private val hideWrongLivid by ConfigDelegate<Boolean>("highlightLivid.hideWrong")
    private val highlightLividLine by ConfigDelegate<Boolean>("highlightLivid.line")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Highlight livid",
                "Highlights the correct Livid in dungeons",
                "Dungeons",
                ConfigElement(
                    "highlightLivid",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Highlight correct livid color",
                ConfigElement(
                    "highlightLivid.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
            .addFeatureOption(
                "Hide incorrect livid entity",
                ConfigElement(
                    "highlightLivid.hideWrong",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Line to correct livid entity",
                ConfigElement(
                    "highlightLivid.line",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        createCustomEvent<RenderEvent.Entity.Pre>("renderLivid") { event ->
            val entity = event.entity

            if (lividEntity == entity && player?.hasLineOfSight(entity) == true) {
                entity.glowThisFrame = true
                entity.glowingColor = highlightLividColor.rgb
            }
        }

        createCustomEvent<RenderEvent.World.Last>("renderLine") { event ->
            lividEntity?.let { entity ->
                if (player?.hasLineOfSight(entity) == true) {
                    Render3D.drawLineToEntity(
                        entity,
                        event.context.consumers(),
                        event.context.matrixStack(),
                        highlightLividColor.toFloatArray(),
                        highlightLividColor.alpha.toFloat()
                    )
                }
            }
        }

        createCustomEvent<RenderEvent.Player.Pre>("renderWrong") { event ->
            if (
                event.entity != lividEntity &&
                //#if MC >= 1.21.9
                //$$ event.entity.nameTag?.string?.contains(" Livid") == true
                //#else
                event.entity.name.contains(" Livid")
                //#endif
                ) {
                event.cancel()
            }
        }

        createCustomEvent<TickEvent.Server>("tick") {
            val world = world ?: return@createCustomEvent
            val state: BlockState = world.getBlockState(lividPos) ?: return@createCustomEvent
            val color = stainedGlassBlocks[state.block] ?: return@createCustomEvent
            val lividType = lividTypes[color] ?: return@createCustomEvent

            world.players().find { it.name.contains(Component.literal(lividType)) }?.let {
                lividEntity = it
                registerRender()
                unregisterEvent("tick")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.") {
                TickScheduler.Server.schedule(80) {
                    registerEvent("tick")
                }
            }
        }

        register<LocationEvent.WorldChange> {
            unregisterRender()
        }
    }

    private fun registerRender() {
        registerEvent("renderLivid")
        if (hideWrongLivid) registerEvent("renderWrong")
        if (highlightLividLine) registerEvent("renderLine")
    }

    private fun unregisterRender() {
        unregisterEvent("renderLivid")
        unregisterEvent("renderWrong")
        unregisterEvent("renderLine")
        lividEntity = null
    }
}

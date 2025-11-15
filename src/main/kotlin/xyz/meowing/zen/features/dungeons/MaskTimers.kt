package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.api.skyblock.PetTracker
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.ItemUtils.createSkull
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.dungeons.DungeonAPI
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.TickEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object MaskTimers : Feature(
    "maskTimers",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private const val NAME = "MaskTimers"
    private val SpiritMask: ItemStack = createSkull("eyJ0aW1lc3RhbXAiOjE1MDUyMjI5OTg3MzQsInByb2ZpbGVJZCI6IjBiZTU2MmUxNzIyODQ3YmQ5MDY3MWYxNzNjNjA5NmNhIiwicHJvZmlsZU5hbWUiOiJ4Y29vbHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn0sInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJiZTcyMWQ3YWQ4YWI5NjVmMDhjYmVjMGI4MzRmNzc5YjUxOTdmNzlkYTRhZWEzZDEzZDI1M2VjZTlkZWMyIn19fQ==")
    private val BonzoMask: ItemStack = createSkull("eyJ0aW1lc3RhbXAiOjE1ODc5MDgzMDU4MjYsInByb2ZpbGVJZCI6IjJkYzc3YWU3OTQ2MzQ4MDI5NDI4MGM4NDIyNzRiNTY3IiwicHJvZmlsZU5hbWUiOiJzYWR5MDYxMCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI3MTZlY2JmNWI4ZGEwMGIwNWYzMTZlYzZhZjYxZThiZDAyODA1YjIxZWI4ZTQ0MDE1MTQ2OGRjNjU2NTQ5YyJ9fX0=")
    private val Phoenix: ItemStack = createSkull("ewogICJ0aW1lc3RhbXAiIDogMTY0Mjg2NTc3MTM5MSwKICAicHJvZmlsZUlkIiA6ICJiYjdjY2E3MTA0MzQ0NDEyOGQzMDg5ZTEzYmRmYWI1OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsYXVyZW5jaW8zMDMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZiMWI1OWJjODkwYzljOTc1Mjc3ODdkZGUyMDYwMGM4Yjg2ZjZiOTkxMmQ1MWE2YmZjZGIwZTRjMmFhM2M5NyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9")

    private val previewMasks = listOf(
        MaskData(BonzoMask, "153.4s", "§c", true),
        MaskData(SpiritMask, "12.4s", "§b", false),
        MaskData(Phoenix, "60.0s", "§6", true)
    )

    private val BonzoRegex = "^Your (?:. )?Bonzo's Mask saved your life!$".toRegex()

    private var BonzoTicks = 0.0
    private var SpiritTicks = 0.0
    private var PhoenixTicks = 0.0

    private var hasSpiritMask = false
    private var hasBonzoMask = false

    data class MaskData(val mask: ItemStack, val timeStr: String, val color: String, val isWearing: Boolean)

    private val tickCall: EventCall = EventBus.register<TickEvent.Server> {
        updateTimers()
        updateHelmetStatus()
    }

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Mask cooldown display",
                "Shows cooldown timers for dungeon masks",
                "Dungeons",
                ConfigElement(
                    "maskTimers",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.registerCustom(NAME, 60, 57, this::editorRender, "maskTimers")

        register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val text = event.message.string.removeFormatting()

            when {
                text.matches(BonzoRegex) -> {
                    val time = if (text.contains("⚚")) 180.0 else 360.0

                    BonzoTicks = (maxOf(180.0, time - DungeonAPI.cataLevel * 3.6) * 20)
                    tickCall.register()
                }
                text == "Second Wind Activated! Your Spirit Mask saved your life!" -> {
                    SpiritTicks = DungeonAPI.getMageReduction(30.0) * 20
                    tickCall.register()
                }
                text == "Your Phoenix Pet saved you from certain death!" -> {
                    PhoenixTicks = 1200.0
                    tickCall.register()
                }
            }
        }

        register<GuiEvent.Render.HUD> { event ->
            render(event.context)
        }

        register<LocationEvent.WorldChange> {
            BonzoTicks = 0.0
            SpiritTicks = 0.0
        }
    }

    private fun updateTimers() {
        if (BonzoTicks > 0) BonzoTicks--
        if (SpiritTicks > 0) SpiritTicks--
        if (PhoenixTicks > 0) PhoenixTicks--
        if (BonzoTicks <= 0 && SpiritTicks <= 0 && PhoenixTicks <= 0) tickCall.unregister()
    }

    private fun updateHelmetStatus() {
        hasSpiritMask = checkHelmet("Spirit Mask")
        hasBonzoMask = checkHelmet("Bonzo's Mask")
    }

    private fun render(context: GuiGraphics) {
        val activeMasks = getActiveMasks()
        if (activeMasks.isEmpty()) return

        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)
        drawHUD(context, x, y, scale, activeMasks)
    }

    private fun editorRender(context: GuiGraphics) = drawHUD(context, 0f, 0f, 1f, previewMasks)

    private fun getActiveMasks(): List<MaskData> {
        val masks = mutableListOf<MaskData>()

        if (BonzoTicks > 0) {
            val timeStr = String.format("%.1fs", BonzoTicks / 20.0)
            masks.add(MaskData(BonzoMask, timeStr, "§c", hasBonzoMask))
        }

        if (SpiritTicks > 0) {
            val timeStr = String.format("%.1fs", SpiritTicks / 20.0)
            masks.add(MaskData(SpiritMask, timeStr, "§b", hasSpiritMask))
        }

        if (PhoenixTicks > 0) {
            val timeStr = String.format("%.1fs", PhoenixTicks / 20.0)
            masks.add(MaskData(Phoenix, timeStr, "§6", PetTracker.name.contains("phoenix", true)))
        }

        return masks
    }

    private fun checkHelmet(name: String): Boolean {
        return player?.inventory?.getItem(39)?.hoverName?.string?.contains(name) == true
    }

    private fun drawHUD(context: GuiGraphics, x: Float, y: Float, scale: Float, masks: List<MaskData>) {
        val iconSize = 16f * scale
        val spacing = 2f * scale
        var currentY = y

        masks.forEach { maskData ->
            val textY = currentY + (iconSize - 8f) / 2f
            val separatorColor = if (maskData.isWearing) "§a" else "§7"

            Render2D.renderItem(context, maskData.mask, x, currentY, scale)
            Render2D.renderStringWithShadow(context, "${separatorColor}| ${maskData.color}${maskData.timeStr}", x + iconSize + spacing, textY, scale)

            currentY += iconSize + spacing
        }
    }
}
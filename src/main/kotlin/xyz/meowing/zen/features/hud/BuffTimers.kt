package xyz.meowing.zen.features.hud

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import xyz.meowing.knit.api.KnitPlayer
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.PacketEvent
import xyz.meowing.zen.events.core.SkyblockEvent
import xyz.meowing.zen.events.core.TickEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.utils.ItemUtils
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting

@Module
object BuffTimers : Feature(
    "buffTimers",
    true
) {
    private const val NAME = "BuffTimers"

    private const val FLARE_DURATION = 180.0 * 20
    private const val RADIANT_MANA_DURATION = 30.0 * 20
    private const val OVERFLUX_PLASMA_DURATION = 60.0 * 20

    private var ragnarock = 0.0
    private var sobh = 0.0
    private var tuba = 0.0
    private var flare = 0.0
    private var orb = 0.0

    private var ragnarockItem: ItemStack? = null
    private var sobhItem: ItemStack? = null
    private var tubaItem: ItemStack? = null
    private var flareItem: ItemStack? = null
    private var orbItem: ItemStack? = null

    private fun previewBuffs(): List<BuffData> {
        return listOf(
            BuffData(ItemStack(Items.GOLDEN_SWORD), "20.0s", "§e"),
            BuffData(ItemStack(Items.WOODEN_SWORD), "5.0s", "§c"),
            BuffData(ItemStack(Items.HOPPER), "Ready", "§7"),
            BuffData(ItemStack(Items.FIREWORK_ROCKET), "2:45", "§4"),
        )
    }

    private val alwaysShow by ConfigDelegate<Boolean>("buffTimers.alwaysShow")
    private val tickCall: EventCall = EventBus.register<TickEvent.Server>(add = false) {
        updateTimers()
        updateItems()
    }

    data class BuffData(val item: ItemStack, val timeStr: String, val color: String)

    override fun addConfig() {
        ConfigManager
            .addFeature(
            "Buff timers",
            "Shows timers for various item buffs and abilities",
            "HUD",
            ConfigElement("buffTimers", ElementType.Switch(false))
        )
            .addFeatureOption(
                "Always show items",
                ConfigElement(
                    "buffTimers.alwaysShow",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        HUDManager.registerCustom(NAME, 60, 75, this::editorRender, "buffTimers")

        register<SkyblockEvent.ItemAbilityUsed> { event ->
            when (event.ability.itemId) {
                "SWORD_OF_BAD_HEALTH" -> if (sobh <= 0) sobh = event.ability.cooldownSeconds * 20
                "WEIRD_TUBA", "WEIRDER_TUBA" -> if (tuba <= 0) tuba = event.ability.cooldownSeconds * 20
            }
            tickCall.register()
        }

        register<EntityEvent.Interact> { event ->
            if (event.action != "USE_ITEM") return@register
            val held = KnitPlayer.player?.mainHandItem ?: return@register
            val id = held.skyblockID

            when {
                id.contains("FLARE") && flare <= 0 -> {
                    flare = FLARE_DURATION
                    tickCall.register()
                }
                id.contains("POWER_ORB") && orb <= 0 -> {
                    orb = getOrbDuration(id)
                    tickCall.register()
                }
            }
        }

        register<PacketEvent.Received> { event ->
            if (event.packet is ClientboundSoundPacket) {
                val packet = event.packet
                if (
                    packet.sound.toString().contains("minecraft:entity.wolf.death") &&
                    packet.pitch == 1.4920635f && ItemUtils.isHolding("RAGNAROCK_AXE")
                ) {
                    ragnarock = 400.0
                    tickCall.register()
                }
            }
        }

        register<ChatEvent.Receive> { event ->
            val msg = event.message.string.removeFormatting()
            when {
                msg.contains("You cannot use this item in the village!") && flare > 0 -> flare = 0.0
                msg.contains("Your flare disappeared") && flare > 0 -> flare = 0.0
                msg.contains("Your previous") && msg.contains("was removed") -> {
                    when {
                        msg.contains("Flare") && flare > 0 -> flare = FLARE_DURATION
                        msg.contains("Power Orb") && orb > 0 -> orbItem?.let {
                            orb = getOrbDuration(it.skyblockID)
                        }
                    }
                    tickCall.register()
                }
            }
        }

        register<GuiEvent.Render.HUD> { event ->
            render(event.context)
        }

        register<LocationEvent.WorldChange> { reset() }
    }

    private fun reset() {
        ragnarock = 0.0
        sobh = 0.0
        tuba = 0.0
        flare = 0.0
        orb = 0.0
        ragnarockItem = null
        sobhItem = null
        tubaItem = null
        flareItem = null
        orbItem = null
    }

    private fun getOrbDuration(id: String) = when {
        id.contains("RADIANT") || id.contains("MANA_FLUX") -> RADIANT_MANA_DURATION
        id.contains("OVERFLUX") || id.contains("PLASMAFLUX") -> OVERFLUX_PLASMA_DURATION
        else -> RADIANT_MANA_DURATION
    }

    private fun updateTimers() {
        if (ragnarock > 0) ragnarock--
        if (sobh > 0) sobh--
        if (tuba > 0) tuba--
        if (flare > 0) flare--
        if (orb > 0) orb--

        if (ragnarock <= 0 && sobh <= 0 && tuba <= 0 && flare <= 0 && orb <= 0) {
            tickCall.unregister()
        }
    }

    private fun updateItems() {
        ragnarockItem = null; sobhItem = null; tubaItem = null; flareItem = null; orbItem = null

        KnitPlayer.player?.inventory?.let { inventory ->
            for (slot in 0..35) {
                val stack = inventory.getItem(slot)
                val id = stack.skyblockID

                when {
                    id == "RAGNAROCK_AXE" && ragnarockItem == null -> ragnarockItem = stack
                    id == "SWORD_OF_BAD_HEALTH" && sobhItem == null -> sobhItem = stack
                    (id == "WEIRD_TUBA" || id == "WEIRDER_TUBA") && tubaItem == null -> tubaItem = stack
                    id.contains("FLARE") && flareItem == null -> flareItem = stack
                    id.contains("POWER_ORB") && orbItem == null -> orbItem = stack
                }
            }
        }
    }

    private fun render(context: GuiGraphics) {
        val activeBuffs = getActiveBuffs()
        if (activeBuffs.isEmpty()) return

        val x = HUDManager.getX(NAME)
        val y = HUDManager.getY(NAME)
        val scale = HUDManager.getScale(NAME)
        drawHUD(context, x, y, scale, activeBuffs)
    }

    private fun editorRender(context: GuiGraphics) = drawHUD(context, 0f, 0f, 1f, previewBuffs())

    private fun getActiveBuffs(): List<BuffData> {
        val buffs = mutableListOf<BuffData>()

        addBuff(ragnarockItem, ragnarock, "§6", buffs)
        addBuff(sobhItem, sobh, "§c", buffs)
        addBuff(tubaItem, tuba, "§7", buffs)
        addBuff(flareItem, flare, "§4", buffs)
        addBuff(orbItem, orb, "§4", buffs)

        return buffs
    }

    fun addBuff(item: ItemStack?, timer: Double, color: String, buffs: MutableList<BuffData>) {
        item?.let {
            if (timer > 0 || alwaysShow) {
                val timeStr = if (timer > 0) formatTicks(timer) else "Ready"
                buffs.add(BuffData(it, timeStr, color))
            }
        }
    }

    private fun formatTicks(t: Double): String {
        val seconds = t / 20.0
        return if (seconds >= 60) {
            "%d:%02d".format(seconds.toInt() / 60, seconds.toInt() % 60)
        } else {
            "%.1fs".format(seconds)
        }
    }

    private fun drawHUD(context: GuiGraphics, x: Float, y: Float, scale: Float, buffs: List<BuffData>) {
        val iconSize = 16f * scale
        val spacing = 2f * scale
        var currentY = y

        buffs.forEach { buffData ->
            val textY = currentY + (iconSize - 8f) / 2f
            val separatorColor = "§7"

            Render2D.renderItem(context, buffData.item, x, currentY, scale)
            Render2D.renderStringWithShadow(context, "${separatorColor}| ${buffData.color}${buffData.timeStr}", x + iconSize + spacing, textY, scale)

            currentY += iconSize + spacing
        }
    }
}
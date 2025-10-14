package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.PacketEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.ItemUtils.getSBStrength
import xyz.meowing.zen.utils.ItemUtils.isHolding
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.util.Hand

@Zen.Module
object RagnarockAlert : Feature("ragalert", true) {
    private val ragparty by ConfigDelegate<Boolean>("ragparty")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Ragnarok alert", "", "General", xyz.meowing.zen.ui.ConfigElement(
                "ragalert",
                ElementType.Switch(false)
            ))
            .addFeatureOption("Send party message", "Send party message", "Options", xyz.meowing.zen.ui.ConfigElement(
                "ragparty",
                ElementType.Switch(false)
            ))
        return configUI
    }

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is PlaySoundS2CPacket) {
                val packet = event.packet
                if (!packet.sound.toString().contains("minecraft:entity.wolf.death") || packet.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@register
                val strengthGain = ((player?.getStackInHand(Hand.MAIN_HAND)?.getSBStrength ?: return@register) * 1.5).toInt()
                showTitle("§cRag §fCasted!", "§c❁ Strength:§b $strengthGain", 2000)
                if (ragparty) ChatUtils.command("pc Strength from Ragnarok: $strengthGain")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "Ragnarock was cancelled due to taking damage!") {
                showTitle("§cRag §4Cancelled!", null, 2000)
            }
        }
    }
}
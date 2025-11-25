package xyz.meowing.zen.features.general

import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.elements.base.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ItemUtils.getSBStrength
import xyz.meowing.zen.utils.ItemUtils.isHolding
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.world.InteractionHand
import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.PacketEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object RagnarockAlert : Feature(
    "ragAlert",
    true
) {
    private val ragParty by ConfigDelegate<Boolean>("ragAlert.party")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Ragnarok alert",
                "Show alert on ragnarok cast",
                "General",
                ConfigElement(
                    "ragAlert",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Send party message",
                ConfigElement(
                    "ragAlert.party",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is ClientboundSoundPacket) {
                val packet = event.packet
                if (!packet.sound.toString().contains("minecraft:entity.wolf.death") || packet.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@register
                val strengthGain = ((player?.getItemInHand(InteractionHand.MAIN_HAND)?.getSBStrength ?: return@register) * 1.5).toInt()

                showTitle("§cRag §fCasted!", "§c❁ Strength:§b $strengthGain", 2000)
                if (ragParty) KnitChat.sendCommand("pc Strength from Ragnarok: $strengthGain")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "Ragnarock was cancelled due to taking damage!") {
                showTitle("§cRag §4Cancelled!", null, 2000)
            }
        }
    }
}
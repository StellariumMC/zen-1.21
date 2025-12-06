package xyz.meowing.zen.features.general

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

@Module
object RagnarockAlert : Feature(
    "ragAlert",
    "Ragnarok alert",
    "Show alert on ragnarok cast",
    "General",
    skyblockOnly = true
) {
    private val sendToParty by config.switch("Send to party")

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is ClientboundSoundPacket) {
                val packet = event.packet
                if (!packet.sound.toString().contains("minecraft:entity.wolf.death") || packet.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@register
                val strengthGain = ((player?.getItemInHand(InteractionHand.MAIN_HAND)?.getSBStrength ?: return@register) * 1.5).toInt()

                showTitle("§cRag §fCasted!", "§c❁ Strength:§b $strengthGain", 2000)
                if (sendToParty) KnitChat.sendCommand("pc Strength from Ragnarok: $strengthGain")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.message.string.removeFormatting() == "Ragnarock was cancelled due to taking damage!") {
                showTitle("§cRag §4Cancelled!", null, 2000)
            }
        }
    }
}
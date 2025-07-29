package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.EntityEvent
import meowing.zen.utils.TitleUtils.showTitle
import net.minecraft.entity.decoration.ArmorStandEntity

@Zen.Module
object KeyAlert : Feature("keyalert", area = "catacombs") {
    private var bloodOpen = false

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Key Spawn Alert", ConfigElement(
                "keyalert",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val message = event.message.string
            if (!bloodOpen && message.startsWith("[BOSS] The Watcher: ")) bloodOpen = true
        }

        register<EntityEvent.Join> { event ->
            if (bloodOpen) return@register
            if (event.entity !is ArmorStandEntity) return@register
            TickUtils.scheduleServer(2) {
                val name = event.entity.name?.string?.removeFormatting() ?: return@scheduleServer
                when {
                    name.contains("Wither Key") -> showTitle("§8Wither §fkey spawned!", null, 2000)
                    name.contains("Blood Key") -> showTitle("§cBlood §fkey spawned!", null, 2000)
                }
            }
        }
    }

    override fun onRegister() {
        bloodOpen = false
        super.onRegister()
    }

    override fun onUnregister() {
        bloodOpen = false
        super.onUnregister()
    }
}

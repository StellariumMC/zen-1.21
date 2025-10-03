package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.utils.TitleUtils.showTitle
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
            if (!bloodOpen && event.message.string.removeFormatting().startsWith("[BOSS] The Watcher: ")) bloodOpen = true
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

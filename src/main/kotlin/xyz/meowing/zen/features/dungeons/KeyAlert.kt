package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import xyz.meowing.zen.utils.TitleUtils.showTitle
import net.minecraft.world.entity.decoration.ArmorStand
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object KeyAlert : Feature(
    "keySpawnAlert",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private var bloodOpen = false

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Key spawn alert",
                "Displays a title when a Key spawns in dungeons",
                "Dungeons",
                ConfigElement(
                    "keySpawnAlert",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (!bloodOpen && event.message.string.removeFormatting().startsWith("[BOSS] The Watcher: ")) bloodOpen = true
        }

        register<EntityEvent.Join> { event ->
            if (bloodOpen) return@register
            if (event.entity !is ArmorStand) return@register
            TickUtils.scheduleServer(2) {
                val name = event.entity.name?.string?.removeFormatting() ?: return@scheduleServer
                when {
                    name.contains("Wither Key") -> showTitle("§8Wither §fkey spawned!", null, 2000)
                    name.contains("Blood Key") -> showTitle("§cBlood §fkey spawned!", null, 2000)
                }
            }
        }

        register<LocationEvent.WorldChange> { bloodOpen = false }
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

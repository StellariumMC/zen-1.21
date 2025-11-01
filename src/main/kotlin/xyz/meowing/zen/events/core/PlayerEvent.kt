@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.item.ItemStack
import xyz.meowing.knit.api.events.Event

sealed class PlayerEvent {
    /**
     * Posted when a change in the current player hotbar has occurred.
     *
     * @see xyz.meowing.zen.events.compat.SkyblockAPI
     * @since 1.2.0
     */
    class HotbarChange(
        val slot: Int,
        val item: ItemStack
    ) : Event()
}
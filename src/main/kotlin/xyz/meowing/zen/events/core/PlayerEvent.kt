@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.item.ItemStack
import xyz.meowing.knit.api.events.Event

sealed class PlayerEvent {
    class HotbarChange(
        val slot: Int,
        val item: ItemStack
    ) : Event()
}
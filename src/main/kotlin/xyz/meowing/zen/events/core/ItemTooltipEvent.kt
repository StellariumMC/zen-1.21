@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.network.chat.Component
import xyz.meowing.knit.api.events.Event

/**
 * Posted after the game has appended all base tooltip lines to the list.
 *
 * @see xyz.meowing.knit.api.events.EventBus
 * @since 1.2.0
 */
class ItemTooltipEvent(
    val stack: ItemStack,
    val context: Item.TooltipContext,
    val type: TooltipFlag,
    val lines: MutableList<Component>
) : Event()

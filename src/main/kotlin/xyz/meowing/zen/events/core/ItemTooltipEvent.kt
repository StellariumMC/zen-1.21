@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
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
    val type: TooltipType,
    val lines: MutableList<Text>
) : Event()

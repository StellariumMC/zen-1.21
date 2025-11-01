@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import xyz.meowing.knit.api.events.CancellableEvent
import xyz.meowing.knit.api.events.Event

sealed class GuiEvent {
    sealed class Render {
        /**
         * Posted after the inGameHud has finished rendering.
         *
         * @see xyz.meowing.zen.mixins.MixinGameRenderer
         * @since 1.2.0
         */
        class HUD(
            val context: DrawContext
        ) : Event()

        /**
         * Posted for the elements using NanoVG to render with the NanoVG beginFrame and endFrame already setup.
         *
         * @see xyz.meowing.vexel.mixins.MixinGameRenderer
         * @since 1.2.0
         */
        class NVG : Event()

        /**
         * Posted when everything has finished rendering.
         *
         * @see xyz.meowing.knit.api.events.EventBus
         * @since 1.2.0
         */
        class Post(
            val screen: Screen,
            val context: DrawContext
        ) : Event()
    }

    /**
     * Posted when a Screen has started opening.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Open(
        val screen: Screen
    ) : Event()

    /**
     * Posted when the current Screen has started closing.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Close(
        val screen: Screen,
        val handler: ScreenHandler
    ) : CancellableEvent()

    /**
     * Posted when a mouse button is clicked inside a Screen.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class Click(
        val mouseX: Double,
        val mouseY: Double,
        val mouseButton: Int,
        val buttonState: Boolean,
        val screen: Screen
    ) : CancellableEvent()

    /**
     * Posted when a keyboard button clicked inside a Screen.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @see xyz.meowing.zen.mixins.MixinKeyboard
     * @since 1.2.0
     */
    class Key(
        val keyName: String?,
        val key: Int,
        val character: Char,
        val scanCode: Int,
        val screen: Screen
    ) : CancellableEvent()

    sealed class Slot {
        /**
         * Posted when a Slot is clicked with a mouse button.
         *
         * @see xyz.meowing.zen.mixins.MixinHandledScreen
         * @since 1.2.0
         */
        class Click(
            val slot: net.minecraft.screen.slot.Slot?,
            val slotId: Int,
            val button: Int,
            val actionType: SlotActionType,
            val handler: ScreenHandler,
            val screen: HandledScreen<*>
        ) : CancellableEvent()

        /**
         * Posted when a Slot is being rendered.
         *
         * @see xyz.meowing.zen.mixins.MixinHandledScreen
         * @since 1.2.0
         */
        class Render(
            val context: DrawContext,
            val slot: net.minecraft.screen.slot.Slot,
            val screen: HandledScreen<ScreenHandler>
        ) : Event()
    }
}
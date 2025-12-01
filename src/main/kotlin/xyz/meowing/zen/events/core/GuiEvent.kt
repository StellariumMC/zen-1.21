@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
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
        sealed class HUD {
            class Pre(
                val context: GuiGraphics
            ) : Event()

            class Post(
                val context: GuiGraphics
            ) : Event()
        }

        /**
         * Posted for the elements using NanoVG to render with the NanoVG beginFrame and endFrame already setup.
         *
         * @see xyz.meowing.vexel.mixins.MixinGameRenderer
         * @since 1.2.0
         */
        class NVG : Event()
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
        val handler: AbstractContainerMenu
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
     * @see xyz.meowing.zen.mixins.MixinKeyboardHandler
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
         * @see xyz.meowing.zen.mixins.MixinAbstractContainerScreen
         * @since 1.2.0
         */
        class Click(
            val slot: net.minecraft.world.inventory.Slot?,
            val slotId: Int,
            val button: Int,
            val actionType: ClickType,
            val handler: AbstractContainerMenu,
            val screen: AbstractContainerScreen<*>
        ) : CancellableEvent()

        /**
         * Posted when a Slot is being rendered.
         *
         * @see xyz.meowing.zen.mixins.MixinAbstractContainerScreen
         * @since 1.2.0
         */
        class Render(
            val context: GuiGraphics,
            val slot: net.minecraft.world.inventory.Slot,
            val screen: AbstractContainerScreen<AbstractContainerMenu>
        ) : Event()
    }

    enum class RenderType {
        Pre,
        Post
        ;
    }
}
package xyz.meowing.zen.features.general.keyShortcuts

import xyz.meowing.knit.api.KnitChat
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.core.KeyEvent
import xyz.meowing.zen.events.core.MouseEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.managers.config.ConfigElement
import xyz.meowing.zen.managers.config.ConfigManager

@Module
object KeyShortcuts : Feature(
    "keyShortcuts"
) {
    val bindingData = StoredFile("features/KeyShortcuts")
    var bindings: List<KeybindEntry> by bindingData.list("bindings", KeybindEntry.CODEC)

    private val pressedKeys = mutableSetOf<Int>()
    private val pressedMouseButtons = mutableSetOf<Int>()
    private val triggeredBindings = mutableSetOf<KeybindEntry>()

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Key shortcuts",
                "Create custom keybinds to execute commands",
                "General",
                ConfigElement(
                    "keyShortcuts",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Open keybind manager",
                ConfigElement(
                    "keyShortcuts.guiButton",
                    ElementType.Button("Open Manager") {
                        TickScheduler.Client.post {
                            client.setScreen(KeybindGui())
                        }
                    }
                )
            )
    }

    override fun initialize() {
        register<KeyEvent.Press> { event ->
            if (client.screen != null) return@register

            if (event.keyCode > 0) {
                pressedKeys.add(event.keyCode)
                checkKeybindMatch()
            }
        }

        register<KeyEvent.Release> { event ->
            if (event.keyCode > 0) {
                pressedKeys.remove(event.keyCode)
                resetTriggeredBindings()
            }
        }

        register<MouseEvent.Click> { event ->
            if (client.screen != null) return@register

            val mouseCode = -(event.button + 1)
            pressedMouseButtons.add(mouseCode)
            checkKeybindMatch()
        }

        register<MouseEvent.Release> { event ->
            pressedMouseButtons.remove(-(event.button + 1))
            resetTriggeredBindings()
        }
    }

    private fun checkKeybindMatch() {
        val allPressed = pressedKeys + pressedMouseButtons

        bindings.forEach { binding ->
            if (binding.keys.isEmpty()) return@forEach
            val isMatch = binding.keys.all { it in allPressed }

            if (isMatch && binding !in triggeredBindings) {
                triggeredBindings.add(binding)
                val command = binding.command.takeIf { it.isNotEmpty() } ?: return
                if (command.startsWith("/")) KnitChat.sendCommand(command) else KnitChat.sendMessage(command)
            }
        }
    }

    private fun resetTriggeredBindings() {
        val allPressed = pressedKeys + pressedMouseButtons
        triggeredBindings.removeIf { binding ->
            !binding.keys.all { it in allPressed }
        }
    }

    fun addBinding(keys: List<Int>, command: String): Boolean {
        if (command.isBlank() || keys.isEmpty() || bindings.any { it.keys == keys }) return false
        bindings = bindings + KeybindEntry(keys, command)
        bindingData.forceSave()
        return true
    }

    fun removeBinding(index: Int): Boolean {
        if (index < 0 || index >= bindings.size) return false
        bindings = bindings.filterIndexed { i, _ -> i != index }
        bindingData.forceSave()
        return true
    }

    fun updateBinding(index: Int, keys: List<Int>, command: String): Boolean {
        if (index < 0 || index >= bindings.size || command.isBlank() || keys.isEmpty()) return false
        if (bindings.any { it.keys == keys && bindings.indexOf(it) != index }) return false
        bindings = bindings.mapIndexed { i, binding ->
            if (i == index) KeybindEntry(keys, command) else binding
        }
        bindingData.forceSave()
        return true
    }
}
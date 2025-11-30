package xyz.meowing.zen.api.skyblock

import com.mojang.serialization.Codec
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ChestMenu
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.data.StoredFile
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.TickEvent
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting


@Module
object EffectsAPI {
    private val activeEffects = mutableMapOf<String, Int>()
    private val effectData = StoredFile("api/EffectsAPI")
    private var savedEffects by effectData.map("effects", Codec.STRING, Codec.INT, emptyMap())

    fun getTime(effectName: String): Int = activeEffects[effectName] ?: 0
    fun updateTime(effectName: String, seconds: Int) {
        if (seconds <= 0) {
            activeEffects.remove(effectName)
            savedEffects = savedEffects - effectName
        } else {
            activeEffects[effectName] = seconds
            savedEffects = savedEffects + (effectName to seconds)
        }
    }

    fun setRemaining(effectName: String, seconds: Int, save: Boolean = false) {
        updateTime(effectName, seconds)
        if (save) effectData.forceSave()
    }

    fun addTime(effectName: String, secondsToAdd: Int) {
        val current = getTime(effectName)
        setRemaining(effectName, current + secondsToAdd, save = true)
    }

    fun onEffectUpdate(listener: (String, Int) -> Unit) {
        effectUpdate.add(listener)
    }

    private val effectUpdate = mutableListOf<(String, Int) -> Unit>()

    init {
        effectData.reload()
        activeEffects.putAll(savedEffects)

        EventBus.register<GuiEvent.Open> { event ->
            val screen = event.screen as? AbstractContainerScreen<*> ?: return@register
            if (screen.title.string.removeFormatting().contains("Active Effects")) {
                TickUtils.schedule(3L) {
                    scanEffectsMenu(screen)
                }
            }
        }

        EventBus.register<LocationEvent.SkyblockLeave> {
            saveCurrentState()
        }

        var tickCounter = 0
        EventBus.register<TickEvent.Client> {
            if (++tickCounter >= 6000) {
                saveCurrentState()
                tickCounter = 0
            }
        }
    }

    private fun scanEffectsMenu(screen: AbstractContainerScreen<*>) {
        val menu = screen.menu as? ChestMenu ?: return
        var changed = false

        for (slot in 0 until minOf(54, menu.slots.size)) {
            val stack = menu.getSlot(slot).item.takeIf { !it.isEmpty } ?: continue
            val name = stack.hoverName.string.removeFormatting()

            extractTimeFromLore(stack.lore)?.let { seconds ->
                val oldTime = getTime(name)
                if (oldTime != seconds) {
                    if (seconds <= 0) {
                        activeEffects.remove(name)
                        savedEffects = savedEffects - name
                    } else {
                        activeEffects[name] = seconds
                        savedEffects = savedEffects + (name to seconds)
                    }
                    effectUpdate.forEach { it(name, seconds) }
                    changed = true
                }
            }
        }

        if (changed) {
            effectData.forceSave()
        }
    }

    private fun saveCurrentState() {
        savedEffects = activeEffects.toMap()
        effectData.forceSave()
    }

    private fun extractTimeFromLore(lore: List<String>): Int? {
        val text = lore.joinToString(" ") { it.removeFormatting() }
        Regex("Remaining:\\s*(\\d+):(\\d+)(?::(\\d+))?").find(text)?.groups?.let { groups ->
            return when {
                groups[3] != null -> groups[1]!!.value.toInt() * 3600 + groups[2]!!.value.toInt() * 60 + groups[3]!!.value.toInt()
                groups[2] != null -> groups[1]!!.value.toInt() * 60 + groups[2]!!.value.toInt()
                else -> null
            }
        }
        return null
    }
}
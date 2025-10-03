package xyz.meowing.zen.api

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.events.*
import xyz.meowing.zen.mixins.AccessorPlayerInventory
import xyz.meowing.zen.utils.DungeonUtils
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.LocationUtils
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.DungeonUtils.isMage
import xyz.meowing.zen.utils.SimpleTimeMark
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.item.ItemStack

@Zen.Module
object ItemAbility {
    private val cooldowns = hashMapOf<String, CooldownItem>()
    private val activeCooldowns = hashMapOf<String, Double>()
    private var justUsedAbility: ItemAbility? = null
    private var cooldownReduction = -1

    data class CooldownItem(
        var sneakRightClick: ItemAbility? = null,
        var sneakLeftClick: ItemAbility? = null,
        var rightClick: ItemAbility? = null,
        var leftClick: ItemAbility? = null
    )

    data class ItemAbility(
        val itemId: String,
        var cooldownSeconds: Double = 0.0,
        var currentCount: Double = 0.0,
        var manaCost: Int = 0,
        var usedAt: SimpleTimeMark = TimeUtils.now,
        var abilityName: String = "Unknown",
        var type: String? = null
    )

    private fun sendItemAbilityEvent(ability: ItemAbility) {
        if (ability.manaCost > PlayerStats.mana) return

        EventBus.post(SkyblockEvent.ItemAbilityUsed(ability))
        justUsedAbility = ability
        activeCooldowns[ability.abilityName] = ability.cooldownSeconds
    }

    init {
        TickUtils.loop(10) {
            if (mc.player == null || mc.world == null) return@loop

            activeCooldowns.replaceAll { _, cooldown -> updateCooldown(cooldown) }
            activeCooldowns.clear()

            for (i in 0..7) {
                val inventory = (mc.player?.inventory as? AccessorPlayerInventory)?.main ?: return@loop
                if (inventory[i].isEmpty) continue

                val stack: ItemStack = inventory[i]
                setStackCooldown(stack)
                val skyblockId: String? = stack.skyblockID

                if (skyblockId != null && cooldowns[skyblockId] != null) {
                    val cdSeconds = cooldowns[skyblockId]?.rightClick?.cooldownSeconds ?: 0.0
                    val abilityName = cooldowns[skyblockId]?.rightClick?.abilityName ?: "Unknown"
                    activeCooldowns[abilityName] = cdSeconds / 2.0
                }
            }
        }

        EventBus.register<WorldEvent.Change> ({
            activeCooldowns.clear()
            cooldowns.clear()
            cooldownReduction = -1
        })

        EventBus.register<MouseEvent.Click> ({ event ->
            if (mc.world == null) return@register
            val heldItem = mc.player?.mainHandStack ?: return@register
            val skyblockId = heldItem.skyblockID
            val cdItem = cooldowns[skyblockId] ?: return@register
            val sneaking = mc.player?.isSneaking

            if (event.button == 0) {
                if (sneaking == true) {
                    cdItem.sneakLeftClick?.let { sendItemAbilityEvent(it) }
                } else if (cdItem.leftClick != null) {
                    sendItemAbilityEvent(cdItem.leftClick!!)
                }
            }
        })

        EventBus.register<EntityEvent.Interact> ({ event ->
            if (mc.world == null) return@register
            val heldItem = mc.player?.mainHandStack ?: return@register
            val skyblockId = heldItem.skyblockID
            val cdItem = cooldowns[skyblockId] ?: return@register

            if (mc.player?.isSneaking == true) cdItem.sneakRightClick?.let {
                sendItemAbilityEvent(it)
            } else if (cdItem.rightClick != null) {
                sendItemAbilityEvent(cdItem.rightClick!!)
            }
        })

        EventBus.register<GameEvent.ActionBar> ({ event ->
            val clean = event.message.string.removeFormatting()

            if (clean.startsWith("Used") && LocationUtils.checkArea("catacombs"))
                justUsedAbility = ItemAbility("Dungeon_Ability")

            justUsedAbility?.let { ability ->
                val skyblockId = mc.player?.mainHandStack?.skyblockID ?: return@register
                if (ability.itemId == skyblockId && clean.startsWith("This ability is on cooldown for") && ability.usedAt.since.millis <= 300) {
                    val currentCooldown = clean.replace("[^0-9]".toRegex(), "").toInt()
                    ability.currentCount = ability.cooldownSeconds - currentCooldown
                    activeCooldowns[ability.abilityName] = currentCooldown.toDouble()
                }
            }
        })
    }

    private fun setItemAbility(line: String, cdItem: CooldownItem, skyblockId: String) {
        val abilityName = line.split(": ")[1].split(" {2}")[0]
        val ability = ItemAbility(skyblockId, abilityName = abilityName)

        when {
            line.endsWith("RIGHT CLICK") -> cdItem.rightClick = ability
            line.endsWith("LEFT CLICK") -> cdItem.leftClick = ability
            line.endsWith("SNEAK RIGHT CLICK") -> cdItem.sneakRightClick = ability
            line.endsWith("SNEAK LEFT CLICK") -> cdItem.sneakLeftClick = ability
        }
    }

    private fun setCooldownSeconds(clean: String, cdItem: CooldownItem) {
        val cooldownSeconds = clean.replace("[^0-9]".toRegex(), "").toInt().toDouble()
        listOfNotNull(cdItem.rightClick, cdItem.leftClick, cdItem.sneakRightClick, cdItem.sneakLeftClick).forEach { it.cooldownSeconds = cooldownSeconds }
    }

    private fun setManaCost(clean: String, cdItem: CooldownItem) {
        val manaCost = clean.replace("[^0-9]".toRegex(), "").toInt()
        listOfNotNull(cdItem.rightClick, cdItem.leftClick, cdItem.sneakRightClick, cdItem.sneakLeftClick).forEach { it.manaCost = manaCost }
    }

    private fun setStackCooldown(item: ItemStack) {
        if (mc.world == null) return
        val skyblockId = item.skyblockID
        if (cooldowns.containsKey(skyblockId)) return
        val cdItem = CooldownItem()

        item.lore.forEach { line ->
            val clean = line.removeFormatting()
            when {
                clean.contains("Ability: ") -> setItemAbility(clean, cdItem, skyblockId)
                clean.contains("Cooldown: ") -> setCooldownSeconds(clean, cdItem)
                clean.contains("Mana Cost: ") -> setManaCost(clean, cdItem)
            }
        }

        if (listOf(cdItem.rightClick, cdItem.leftClick, cdItem.sneakRightClick, cdItem.sneakLeftClick).any { it != null }) {
            cooldowns[skyblockId] = cdItem
        }
    }

    private fun updateCooldown(cooldownCount: Double): Double {
        var secondsToAdd = 0.05

        if (LocationUtils.checkArea("catacombs") && cooldownReduction == -1 && isMage()) {
            cooldownReduction = (DungeonUtils.getCurrentLevel() / 2) + 25
            if (!DungeonUtils.isDuplicate("mage")) cooldownReduction += 25
        }

        if (cooldownReduction != -1) secondsToAdd *= (100.0 + cooldownReduction) / cooldownReduction

        return cooldownCount - secondsToAdd
    }
}
package xyz.meowing.zen.api.item

import xyz.meowing.zen.events.*
import xyz.meowing.zen.mixins.AccessorInventory
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.SimpleTimeMark
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TimeUtils.millis
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.world.item.ItemStack
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.api.skyblock.PlayerStats
import xyz.meowing.zen.api.dungeons.DungeonAPI
import xyz.meowing.zen.api.dungeons.DungeonClass
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.MouseEvent
import xyz.meowing.zen.events.core.SkyblockEvent

@Module
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
            if (player == null || world == null) return@loop

            activeCooldowns.replaceAll { _, cooldown -> updateCooldown(cooldown) }
            activeCooldowns.clear()

            for (i in 0..7) {
                val inventory = (player?.inventory as? AccessorInventory)?.main ?: return@loop
                if (inventory[i].isEmpty) continue

                val stack: ItemStack = inventory[i]
                setStackCooldown(stack)
                val skyblockId: String = stack.skyblockID

                if (skyblockId.isNotEmpty() && cooldowns[skyblockId] != null) {
                    val cdSeconds = cooldowns[skyblockId]?.rightClick?.cooldownSeconds ?: 0.0
                    val abilityName = cooldowns[skyblockId]?.rightClick?.abilityName ?: "Unknown"
                    activeCooldowns[abilityName] = cdSeconds / 2.0
                }
            }
        }

        EventBus.register<LocationEvent.WorldChange> {
            activeCooldowns.clear()
            cooldowns.clear()
            cooldownReduction = -1
        }

        EventBus.register<MouseEvent.Click> { event ->
            if (world == null) return@register
            val heldItem = player?.mainHandItem ?: return@register
            val skyblockId = heldItem.skyblockID
            val cdItem = cooldowns[skyblockId] ?: return@register
            val sneaking = player?.isShiftKeyDown

            if (event.button == 0) {
                if (sneaking == true) {
                    cdItem.sneakLeftClick?.let { sendItemAbilityEvent(it) }
                } else if (cdItem.leftClick != null) {
                    sendItemAbilityEvent(cdItem.leftClick!!)
                }
            }
        }

        EventBus.register<EntityEvent.Interact> {
            if (world == null) return@register
            val heldItem = player?.mainHandItem ?: return@register
            val skyblockId = heldItem.skyblockID
            val cdItem = cooldowns[skyblockId] ?: return@register

            if (player?.isShiftKeyDown == true) cdItem.sneakRightClick?.let {
                sendItemAbilityEvent(it)
            } else if (cdItem.rightClick != null) {
                sendItemAbilityEvent(cdItem.rightClick!!)
            }
        }

        EventBus.register<ChatEvent.Receive> { event ->
            if (event.isActionBar) return@register
            val clean = event.message.string.removeFormatting()

            if (clean.startsWith("Used") && SkyBlockIsland.THE_CATACOMBS.inIsland())
                justUsedAbility = ItemAbility("Dungeon_Ability")

            justUsedAbility?.let { ability ->
                val skyblockId = player?.mainHandItem?.skyblockID ?: return@register
                if (ability.itemId == skyblockId && clean.startsWith("This ability is on cooldown for") && ability.usedAt.since.millis <= 300) {
                    val currentCooldown = clean.replace("[^0-9]".toRegex(), "").toInt()
                    ability.currentCount = ability.cooldownSeconds - currentCooldown
                    activeCooldowns[ability.abilityName] = currentCooldown.toDouble()
                }
            }
        }
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
        if (world == null) return
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

        if (SkyBlockIsland.THE_CATACOMBS.inIsland() && cooldownReduction == -1 && DungeonAPI.dungeonClass == DungeonClass.MAGE) {
            cooldownReduction = (DungeonAPI.classLevel / 2) + 25
            if (DungeonAPI.uniqueClass) cooldownReduction += 25
        }

        if (cooldownReduction != -1) secondsToAdd *= (100.0 + cooldownReduction) / cooldownReduction

        return cooldownCount - secondsToAdd
    }
}
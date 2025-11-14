package xyz.meowing.zen.utils

import com.mojang.authlib.GameProfile
import xyz.meowing.zen.utils.Utils.getPlayerTexture
import xyz.meowing.zen.utils.Utils.getPlayerUuid
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import xyz.meowing.knit.api.KnitPlayer.player
import java.util.UUID
import kotlin.random.Random

// Modified from Odin 1.8.9
// https://github.com/odtheking/Odin/blob/main/src/main/kotlin/me/odinmain/utils/skyblock/ItemUtils.kt
object ItemUtils {
    val strengthRegex = Regex("Strength: \\+(\\d+)")
    val abilityRegex = Regex("Ability:.*RIGHT CLICK")

    inline val ItemStack?.extraAttributes: CompoundTag?
        get() = this?.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)?.copyTag()

    inline val ItemStack?.skyblockID: String get() = this?.extraAttributes?.getString("id")?.orElse("") ?: ""

    inline val ItemStack?.lore: List<String> get() = this?.get(DataComponents.LORE)?.lines?.map { it.string } ?: emptyList()

    inline val ItemStack?.uuid: String get() = this?.extraAttributes?.getString("uuid")?.orElse("") ?: ""

    inline val ItemStack?.hasAbility: Boolean get() = this?.lore?.any { abilityRegex.containsMatchIn(it) } == true

    inline val ItemStack?.getSBStrength: Int
        get() = this?.lore?.asSequence()
            ?.map { it.removeFormatting() }
            ?.firstOrNull { it.startsWith("Strength:") }
            ?.let { strengthRegex.find(it)?.groups?.get(1)?.value?.toIntOrNull() } ?: 0

    inline val ItemStack?.isShortbow: Boolean get() = this?.lore?.any { "Shortbow: Instantly shoots!" in it } == true

    fun isHolding(vararg id: String): Boolean = player?.mainHandItem?.skyblockID in id

    fun ItemStack.displayName(): String = this.get(DataComponents.CUSTOM_NAME)?.string ?: this.hoverName.string

    fun createSkull(texture: String, displayName: String? = null, lore: List<String> = emptyList()): ItemStack {
        val uuid = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx".replace("x".toRegex()) {
            Random.nextInt(16).toString(16)
        }
        val profile = GameProfile(UUID.fromString(uuid), uuid)

        try {
            profile.properties.put("textures", com.mojang.authlib.properties.Property("textures", texture))
        } catch (_: UnsupportedOperationException) { }

        return ItemStack(Items.PLAYER_HEAD).apply {
            set(
                DataComponents.PROFILE,
                //#if MC >= 1.21.9
                //$$ ResolvableProfile.createResolved(profile)
                //#else
                ResolvableProfile(profile)
                //#endif
            )
            displayName?.let {
                set(DataComponents.CUSTOM_NAME, Component.literal(it))
            }
            if (lore.isNotEmpty()) {
                set(DataComponents.LORE, ItemLore(
                    lore.map { Component.literal(it) }
                ))
            }
        }
    }

    fun createPlayerSkullByName(
        playerName: String,
        displayName: String? = null,
        lore: List<String> = emptyList(),
        onComplete: (ItemStack?) -> Unit
    ) {
        getPlayerUuid(playerName,
            onSuccess = { uuid ->
                getPlayerTexture(uuid,
                    onSuccess = { texture ->
                        onComplete(createSkull(texture, displayName, lore))
                    },
                    onError = { onComplete(null) }
                )
            },
            onError = { onComplete(null) }
        )
    }
}
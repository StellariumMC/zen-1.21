package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import java.util.UUID
import kotlin.random.Random

// Modified from Odin 1.8.9
// https://github.com/odtheking/Odin/blob/main/src/main/kotlin/me/odinmain/utils/skyblock/ItemUtils.kt
object ItemUtils {
    val strengthRegex = Regex("Strength: \\+(\\d+)")
    val abilityRegex = Regex("Ability:.*RIGHT CLICK")

    inline val ItemStack?.extraAttributes: NbtCompound?
        get() = this?.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)?.copyNbt()

    inline val ItemStack?.skyblockID: String get() = this?.extraAttributes?.getString("id")?.orElse("") ?: ""

    inline val ItemStack?.lore: List<String> get() = this?.get(DataComponentTypes.LORE)?.lines?.map { it.string } ?: emptyList()

    inline val ItemStack?.uuid: String get() = this?.extraAttributes?.getString("uuid")?.orElse("") ?: ""

    inline val ItemStack?.hasAbility: Boolean get() = this?.lore?.any { abilityRegex.containsMatchIn(it) } == true

    inline val ItemStack?.getSBStrength: Int
        get() = this?.lore?.asSequence()
            ?.map { it.removeFormatting() }
            ?.firstOrNull { it.startsWith("Strength:") }
            ?.let { strengthRegex.find(it)?.groups?.get(1)?.value?.toIntOrNull() } ?: 0

    inline val ItemStack?.isShortbow: Boolean get() = this?.lore?.any { "Shortbow: Instantly shoots!" in it } == true

    fun isHolding(vararg id: String): Boolean = mc.player?.mainHandStack?.skyblockID in id

    fun ItemStack.displayName(): String = this.get(DataComponentTypes.CUSTOM_NAME)?.string ?: this.name.string


    fun createSkull(texture: String): ItemStack {
        val uuid = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx".replace("x".toRegex()) {
            Random.nextInt(16).toString(16)
        }
        val profile = com.mojang.authlib.GameProfile(UUID.fromString(uuid), uuid)
        profile.properties.put("textures", com.mojang.authlib.properties.Property("textures", texture))
        return ItemStack(Items.PLAYER_HEAD).apply {
            set(DataComponentTypes.PROFILE, ProfileComponent(profile))
        }
    }
}
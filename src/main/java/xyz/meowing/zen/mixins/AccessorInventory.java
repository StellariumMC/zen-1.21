package xyz.meowing.zen.mixins;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Inventory.class)
public interface AccessorInventory {
    @Accessor("items")
    NonNullList<ItemStack> getMain();
}
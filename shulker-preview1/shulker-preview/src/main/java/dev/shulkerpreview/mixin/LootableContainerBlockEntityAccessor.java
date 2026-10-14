package dev.shulkerpreview.mixin;

import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Liest die Item-Liste direkt, ohne dass Loot-Tabellen generiert werden (keine Weltveränderung). */
@Mixin(ShulkerBoxBlockEntity.class)
public interface LootableContainerBlockEntityAccessor {
    @Invoker("getHeldStacks")
    DefaultedList<ItemStack> shulkerpreview$getHeldStacks();
}

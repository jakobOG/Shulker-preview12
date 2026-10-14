package dev.shulkerpreview;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/** Liest Shulker-Inhalte ausschließlich aus vorhandenen Item-Daten (nichts wird erraten oder verändert). */
public final class ShulkerData {
    public static final int SIZE = 27;

    private ShulkerData() {}

    /** Gilt für alle Farben und für Shulkerbox-Varianten anderer Mods, die von ShulkerBoxBlock erben. */
    public static boolean isShulker(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    /** 27 Einträge in echter Slot-Reihenfolge, leere Slots = ItemStack.EMPTY. */
    public static List<ItemStack> fromStack(ItemStack shulker) {
        DefaultedList<ItemStack> slots = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
        ContainerComponent container = shulker.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            container.copyTo(slots);
        }
        return new ArrayList<>(slots);
    }

    public static boolean allEmpty(List<ItemStack> stacks) {
        for (ItemStack s : stacks) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }
}

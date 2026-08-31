package com.hfr.technology;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TechnologyItem {
    public final String id;
    public final int metadata;
    public final boolean crafting;
    public final boolean usage;
    public final Item item;
    public final ItemStack itemStack;

    public TechnologyItem(String id, boolean crafting, boolean usage) {
        this.id = id;
        this.metadata = 0;
        this.crafting = crafting;
        this.usage = usage;
        this.item = null;
        this.itemStack = null;
    }

    public TechnologyItem(String id, int metadata, boolean crafting, boolean usage) {
        this.id = id;
        this.metadata = metadata;
        this.crafting = crafting;
        this.usage = usage;
        this.item = null;
        this.itemStack = null;
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || item == null) return false;
        if (stack.getItem() != item) return false;
        if (metadata != 0 && stack.getItemDamage() != metadata) return false;
        return true;
    }
}
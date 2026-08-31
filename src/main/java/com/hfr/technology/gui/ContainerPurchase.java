package com.hfr.technology.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerPurchase extends Container {
    public ContainerPurchase(EntityPlayer player) {
    }

    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
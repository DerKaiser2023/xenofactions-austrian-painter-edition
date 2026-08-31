package com.hfr.technology;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class TechnologyItemHandler {

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent event) {
        if (!TechnologyManager.isEnabled()) return;
        if (event.entityPlayer == null) return;
        ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
        if (stack == null) return;
        if (!TechnologyManager.canUseItem(event.entityPlayer, stack)) {
            event.entityPlayer.addChatMessage(new net.minecraft.util.ChatComponentText("\u00a74Technology Locked\n\u00a77This item requires an unlocked technology.\n\u00a77Research and purchase the technology to use this item."));
            event.setCanceled(true);
        }
    }
}
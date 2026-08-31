package com.hfr.technology;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ResearchBook extends Item {
    public ResearchBook() {
        setUnlocalizedName("research_book");
        setTextureName("hfr:research_book");
        setMaxStackSize(1);
    }

    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.openGui(com.hfr.main.MainRegistry.instance, com.hfr.blocks.ModBlocks.guiID_research, world, 0, 0, 0);
        }
        return stack;
    }
}
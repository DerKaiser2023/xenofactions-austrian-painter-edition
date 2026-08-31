package com.hfr.technology;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PurchaseBlock extends Block {
    public PurchaseBlock() {
        super(Material.iron);
        setBlockName("purchase_block");
        setBlockTextureName("hfr:purchase_block");
        setHardness(2.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(com.hfr.main.MainRegistry.instance, com.hfr.blocks.ModBlocks.guiID_purchase, world, x, y, z);
        }
        return true;
    }
}
package com.hfr.blocks;

import com.hfr.leaderboard.FactionLeaderboards;
import com.hfr.leaderboard.FactionLeaderboards.Metric;
import com.hfr.tileentity.TileEntityFactionLeaderboard;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;

/** Independent spawn furniture: configuration and ordinary removal require faction administration. */
public final class BlockFactionLeaderboard extends BlockContainer {
    public BlockFactionLeaderboard() {
        super(Material.iron);
        setBlockName("faction_leaderboard");
        setHardness(3F);
        setResistance(6000000F);
        setBlockTextureName("hfr:concrete_flat");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityFactionLeaderboard(); }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        world.setBlockMetadataWithNotify(x, y, z, (MathHelper.floor_double(placer.rotationYaw * 4F / 360F + .5D) + 2) & 3, 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity raw = world.getTileEntity(x, y, z);
        if (!(raw instanceof TileEntityFactionLeaderboard)) return true;
        TileEntityFactionLeaderboard tile = (TileEntityFactionLeaderboard) raw;
        if (player.isSneaking() && FactionLeaderboards.canManage(player)) {
            Metric[] metrics = Metric.values();
            tile.configure(metrics[(tile.getMetric().ordinal() + 1) % metrics.length]);
        }
        // Also supplies full text at close range without a GUI or client request packet.
        tile.refresh();
        Metric metric = tile.getMetric();
        player.addChatMessage(new ChatComponentText("Faction leaderboard: " + metric.title));
        if (!metric.unavailable.isEmpty()) player.addChatMessage(new ChatComponentText(metric.unavailable));
        else if (tile.rowCount() == 0) player.addChatMessage(new ChatComponentText("No eligible factions."));
        for (int i = 0; i < tile.rowCount(); i++) {
            NBTTagCompound row = tile.row(i);
            player.addChatMessage(new ChatComponentText(row.getInteger("rank") + ". " + row.getString("name")
                + " - " + row.getString("value") + " " + metric.unit));
        }
        if (FactionLeaderboards.canManage(player))
            player.addChatMessage(new ChatComponentText("Sneak-right-click with an empty hand to change metric. /xc leaderboard for commands."));
        return true;
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
        // Clients do not know their server command authority; the server also checks BreakEvent.
        return world.isRemote || FactionLeaderboards.canManage(player)
            ? super.getPlayerRelativeBlockHardness(player, world, x, y, z) : 0F;
    }

    @Override
    public int getMobilityFlag() { return 2; }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) { return false; }

    @Override
    public boolean canDropFromExplosion(Explosion explosion) { return false; }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) { }
}

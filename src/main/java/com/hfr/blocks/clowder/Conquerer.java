package com.hfr.blocks.clowder;

import com.hfr.blocks.ModBlocks;
import com.hfr.clowder.Clowder;
import com.hfr.clowder.ClowderTerritory;
import com.hfr.clowder.ClowderTerritory.CoordPair;
import com.hfr.clowder.ClowderTerritory.TerritoryMeta;
import com.hfr.command.CommandClowderAdmin;
import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;
import com.hfr.tileentity.clowder.TileEntityConquerer;
import com.hfr.tileentity.clowder.TileEntityFlag;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

public class Conquerer extends BlockContainer {

	public Conquerer(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityConquerer();
	}
	
	@Override
	public int getRenderType(){
		return -1;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
			return true;

		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TileEntityConquerer) {
			TileEntityConquerer flag = (TileEntityConquerer)tile;
			if(flag.owner == null || (!CommandClowderAdmin.WARENABLED && !XFConfig.enableConquestFlagsPeacetime) || !flag.canSeeSky() || !flag.checkBorder(x, z) || !noProximity(world, x, y, z)) {
				world.func_147480_a(x, y, z, false);
				return true;
			}
		}

		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {

		boolean warEnabled = CommandClowderAdmin.WARENABLED;
		boolean peacetimeEnabled = XFConfig.enableConquestFlagsPeacetime;
		boolean canPlace = warEnabled || peacetimeEnabled;

		if (canPlace) {

			int i = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if (i == 0) {
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}
		if (i == 1) {
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		}
		if (i == 2) {
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		}
		if (i == 3) {
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		}

		if (player instanceof EntityPlayer && !world.isRemote) {
			TileEntityConquerer flag = (TileEntityConquerer) world.getTileEntity(x, y, z);

			Clowder clowder = Clowder.getClowderFromPlayer((EntityPlayer) player);
			flag.owner = clowder;
			flag.placerName = player.getCommandSenderName();

			CoordPair loc = ClowderTerritory.getCoordPair(world, x, z);
			TerritoryMeta meta = ClowderTerritory.getMetaFromCoords(loc);
			boolean isWilderness = meta == null || meta.owner == null || meta.owner.zone == ClowderTerritory.Zone.WILDERNESS;
			boolean isEnemyFaction = meta != null && meta.owner != null && meta.owner.zone == ClowderTerritory.Zone.FACTION && meta.owner.owner != clowder;

			boolean validPlacement = false;
			if (warEnabled) {
				if (clowder != null && flag.checkBorder(x, z) && flag.canSeeSky() && noProximity(world, x, y, z)) {
					validPlacement = true;
				}
			} else if (peacetimeEnabled) {
				if (clowder != null && isWilderness && flag.canSeeSky() && noProximity(world, x, y, z)) {
					validPlacement = true;
				}
			}

			if (validPlacement) {
				flag.owner.addPrestigeReq(Clowder.flagReq(), world);
				flag.markDirty();
				if (MainRegistry.logger != null) {
					MainRegistry.logger.log(Level.INFO, "[WAR] " + clowder.name + " placed a claim flag for " + getTargetCityName(world, x, z).replaceAll("§[0-9a-fk-or]", "") + " in " + formatDimension(world) + " at (" + x + ", " + y + ", " + z + ")");
				}
			} else {
				flag.owner = null;
				((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You won't be able to raise this flag. This may be due to:"));
				((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-You not being in any faction"));
				((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-The flag not having sky access"));
				if (warEnabled) {
					((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-The flag not being in a foreign border chunk"));
					((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-No active war with the defending faction, or either side is not raidable"));
				} else if (peacetimeEnabled) {
					if (isEnemyFaction) {
						((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-Wars are enabled; conquest flags can only be placed in enemy territory during war"));
					} else {
						((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-The flag must be placed in wilderness (unclaimed land)"));
					}
				}
				((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "-The flag being too close to another conquest flag"));
				((EntityPlayer) player).inventory.addItemStackToInventory
				(new ItemStack(ModBlocks.clowder_conquerer, 1));
			}
		}

		super.onBlockPlacedBy(world, x, y, z, player, itemStack);
	} else {
			if (player instanceof EntityPlayer && !world.isRemote) {
				TileEntityConquerer flag = (TileEntityConquerer) world.getTileEntity(x, y, z);
				flag.owner = null;
				((EntityPlayer) player).inventory.addItemStackToInventory
						(new ItemStack(ModBlocks.clowder_conquerer, 1));
				if (warEnabled) {
					((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Peacetime enabled!"));
				} else if (peacetimeEnabled) {
					((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Conquest flags are disabled! Enable wars or peacetime conquest flags in config."));
				} else {
					((EntityPlayer) player).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Conquest flags are disabled! Enable wars or peacetime conquest flags in config."));
				}
			}
		}

	}
	
	private String formatDimension(World world) {
		if(world == null || world.provider == null)
			return "dimension 0";
		String name = world.provider.getDimensionName();
		if(name == null || name.trim().isEmpty())
			name = "dimension";
		return name + " (dim " + world.provider.dimensionId + ")";
	}

	private String getTargetCityName(World world, int x, int z) {
		TerritoryMeta meta = ClowderTerritory.getMetaFromIntCoords(world, x, z);
		if(meta != null) {
			String cityName = meta.cityName != null && !meta.cityName.trim().isEmpty() ? meta.cityName : meta.name;
			if(cityName != null && !cityName.trim().isEmpty()) {
				String ownerName = meta.owner != null && meta.owner.owner != null ? meta.owner.owner.name + "'s " : "";
				return EnumChatFormatting.AQUA + ownerName + "city " + cityName.trim();
			}
		}

		return EnumChatFormatting.AQUA + "the city at X:" + x + " / Z:" + z;
	}

	public boolean noProximity(World world, int x, int y, int z) {
		
		int range = 4;
		
		for(int ix = x - range; ix <= x + range; ix++) {
			for(int iy = y - 3; iy <= y + 3; iy++) {
				for(int iz = z - range; iz <= z + range; iz++) {
					
					if(ix == x && iy == y && iz == z)
						continue;
					
					if(world.getBlock(ix, iy, iz) == ModBlocks.clowder_conquerer) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int i)
    {
		TileEntityConquerer flag = (TileEntityConquerer)world.getTileEntity(x, y, z);
		if(flag != null && flag.owner != null) {
			flag.owner.addPrestigeReq(-Clowder.flagReq(), world);
		}
		
		super.breakBlock(world, x, y, z, b, i);
    }
}

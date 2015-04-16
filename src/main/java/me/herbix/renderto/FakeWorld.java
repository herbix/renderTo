package me.herbix.renderto;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

public class FakeWorld implements IBlockAccess {
	
	private IBlockState forBlock;
	private IBlockAccess defaultWorld;
	
	public void setForBlock(IBlockState forBlock) {
		this.forBlock = forBlock;
	}
	
	public void setDefaultWorld(IBlockAccess defaultWorld) {
		this.defaultWorld = defaultWorld;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return defaultWorld.getTileEntity(pos);
	}

	@Override
	public int getCombinedLight(BlockPos pos, int p_175626_2_) {
		return 0;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return forBlock;
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return forBlock.getBlock().getMaterial() == Material.air;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
		return BiomeGenBase.plains;
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return defaultWorld.extendedLevelsInChunkCache();
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return 0;
	}

	@Override
	public WorldType getWorldType() {
		return defaultWorld.getWorldType();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return true;
	}

}

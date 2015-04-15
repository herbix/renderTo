package me.herbix.renderto;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class FakeItem extends Item {
	
	private IBlockState forBlock;
	private World world;
	
	public void setForBlock(IBlockState forBlock) {
		this.forBlock = forBlock;
	}

	public void setWorld(World world) {
		this.world = world;
	}
	
	@Override
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		return forBlock.getBlock().colorMultiplier(world, new BlockPos(0, 0, 0), renderPass);
	}

}

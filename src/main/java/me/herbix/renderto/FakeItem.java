package me.herbix.renderto;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class FakeItem extends Item {
	
	private FakeWorld fakeWorld = new FakeWorld();
	private IBlockState forBlock;
	private World world;
	
	public void setForBlock(IBlockState forBlock) {
		this.forBlock = forBlock;
		fakeWorld.setForBlock(forBlock);
	}

	public void setWorld(World world) {
		this.world = world;
		fakeWorld.setDefaultWorld(world);
	}
	
	@Override
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		return forBlock.getBlock().colorMultiplier(fakeWorld, new BlockPos(0, 0, 0), renderPass);
	}

}

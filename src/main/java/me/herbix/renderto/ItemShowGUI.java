package me.herbix.renderto;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemShowGUI extends Item {

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
		if(worldIn.isRemote) {
			Minecraft.getMinecraft().displayGuiScreen(RenderToMod.gui);
		}
		return super.onItemRightClick(itemStackIn, worldIn, playerIn);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		int use = Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode();
		if(use == -100) {
			tooltip.add(I18n.format("item.renderTo.toolTip.1.mouse", "Left"));
		} else if(use == -99) {
			tooltip.add(I18n.format("item.renderTo.toolTip.1.mouse", "Right"));
		} else {
			tooltip.add(I18n.format("item.renderTo.toolTip.1", GameSettings.getKeyDisplayString(use)));
		}
	}
}

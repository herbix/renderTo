package me.herbix.renderto;

import me.herbix.renderto.api.DefaultRenderToApiRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = "renderto")
public class RenderToMod {

	public static RenderToApiHandler api;
	
	public static Item renderToItem;

	public static CreativeTabs tab = new CreativeTabs("renderto") {
		@Override
		public Item getTabIconItem() {
			return renderToItem;
		}
	};
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		renderToItem = new ItemShowGUI().setCreativeTab(tab).setUnlocalizedName("renderTo").setMaxStackSize(1);
		GameRegistry.registerItem(renderToItem, "render_to"); 
		DefaultRenderToApiRegister.instance = api = new RenderToApiHandler();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("init");
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(renderToItem, 0, new ModelResourceLocation("renderto:render_to", "inventory"));
	}

}

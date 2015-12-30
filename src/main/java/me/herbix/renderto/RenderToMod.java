package me.herbix.renderto;

import java.util.HashMap;
import java.util.Map;

import me.herbix.renderto.api.DefaultRenderToApiRegister;
import me.herbix.renderto.api.IRenderToApiEntry;
import me.herbix.renderto.gui.RenderToGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.Logger;

@Mod(modid = "renderto")
public class RenderToMod implements IRenderToApiEntry {

	public static RenderToApiHandler api;
	
	public static Item renderToItem;
	
	public static Logger logger;

	public static CreativeTabs tab = new CreativeTabs("renderto") {
		@Override
		public Item getTabIconItem() {
			return renderToItem;
		}
	};
	
	public static RenderToGuiScreen gui;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		renderToItem = new ItemShowGUI().setCreativeTab(tab).setUnlocalizedName("renderTo").setMaxStackSize(1);
		GameRegistry.registerItem(renderToItem, "render_to"); 
		DefaultRenderToApiRegister.instance = api = new RenderToApiHandler();
		logger = event.getModLog();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(renderToItem, 0, new ModelResourceLocation("renderto:render_to", "inventory"));
		DefaultRenderToApiRegister.instance.register(this);
		gui = new RenderToGuiScreen(Minecraft.getMinecraft().currentScreen);
	}

	@Override
	public int getVersion() {
		return DefaultRenderToApiRegister.API_VERSION;
	}

	@Override
	public Map<String, Entity> getModEntityEntry(World world) {
		Map<String, Entity> result = new HashMap<String, Entity>();
		DifficultyInstance difficulty = world.getDifficultyForLocation(new BlockPos(0, 0, 0));
		
		EntityPigZombie entityPigMan = new EntityPigZombie(world);
		entityPigMan.onInitialSpawn(difficulty, null);
		entityPigMan.setChild(false);
		result.put("PigZombie", entityPigMan);
		
		EntitySkeleton entitySkeleton = new EntitySkeleton(world);
		entitySkeleton.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
		entitySkeleton.setSkeletonType(0);
		result.put("Skeleton:Normal", entitySkeleton);

		entitySkeleton = new EntitySkeleton(world);
		entitySkeleton.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
		entitySkeleton.setSkeletonType(1);
		result.put("Skeleton:Wither", entitySkeleton);
		
		EntityZombie entityZombie = new EntityZombie(world);
		entityZombie.setVillager(false);
		result.put("Zombie:Normal", entityZombie);

		entityZombie = new EntityZombie(world);
		entityZombie.setVillager(true);
		result.put("Zombie:Villager", entityZombie);
		
		EntityMagmaCube entityMagmaCube = new EntityMagmaCube(world);
		NBTTagCompound nbt = new NBTTagCompound();
		entityMagmaCube.writeEntityToNBT(nbt);
		nbt.setInteger("Size", 2);
		entityMagmaCube.readEntityFromNBT(nbt);
		result.put("MagmaCube:Large", entityMagmaCube);

		entityMagmaCube = new EntityMagmaCube(world);
		nbt.setInteger("Size", 1);
		entityMagmaCube.readEntityFromNBT(nbt);
		result.put("MagmaCube:Medium", entityMagmaCube);

		entityMagmaCube = new EntityMagmaCube(world);
		nbt.setInteger("Size", 0);
		entityMagmaCube.readEntityFromNBT(nbt);
		result.put("MagmaCube:Small", entityMagmaCube);
		
		EntitySlime entitySlime = new EntitySlime(world);
		nbt = new NBTTagCompound();
		entitySlime.writeEntityToNBT(nbt);
		nbt.setInteger("Size", 2);
		entitySlime.readEntityFromNBT(nbt);
		result.put("Slime:Large", entitySlime);

		entitySlime = new EntitySlime(world);
		nbt.setInteger("Size", 1);
		entitySlime.readEntityFromNBT(nbt);
		result.put("Slime:Medium", entitySlime);

		entitySlime = new EntitySlime(world);
		nbt.setInteger("Size", 0);
		entitySlime.readEntityFromNBT(nbt);
		result.put("Slime:Small", entitySlime);
		
		EntityGhast entityGhast = new EntityGhast(world);
		entityGhast.setAttacking(false);
		result.put("Ghast:Normal", entityGhast);

		entityGhast = new EntityGhast(world);
		entityGhast.setAttacking(true);
		result.put("Ghast:Shooting", entityGhast);
		
		EntityVillager entityVillager = new EntityVillager(world);
		entityVillager.setProfession(0);
		result.put("Villager:Farmer", entityVillager);
		
		entityVillager = new EntityVillager(world);
		entityVillager.setProfession(1);
		result.put("Villager:Librarian", entityVillager);

		entityVillager = new EntityVillager(world);
		entityVillager.setProfession(2);
		result.put("Villager:Cleric", entityVillager);
		
		entityVillager = new EntityVillager(world);
		entityVillager.setProfession(3);
		result.put("Villager:Blacksmith", entityVillager);

		entityVillager = new EntityVillager(world);
		entityVillager.setProfession(4);
		result.put("Villager:Butcher", entityVillager);
		
		return result;
	}

}

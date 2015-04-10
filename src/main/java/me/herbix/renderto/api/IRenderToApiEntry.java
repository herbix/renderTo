package me.herbix.renderto.api;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface IRenderToApiEntry {
	
	public int getVersion();

	public Map<String, Entity> getModEntityEntry(World world);
	
}

package me.herbix.renderto;

import java.util.Map;

import me.herbix.renderto.api.DefaultRenderToApiRegister;
import me.herbix.renderto.api.IRenderToApiEntry;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class RenderToApiEntryWrapper implements IRenderToApiEntry {

	private IRenderToApiEntry parent;

	public RenderToApiEntryWrapper(IRenderToApiEntry parent) {
		this.parent = parent;
	}
	
	@Override
	public int getVersion() {
		return DefaultRenderToApiRegister.API_VERSION;
	}

	@Override
	public Map<String, Entity> getModEntityEntry(World world) {
		if(parent.getVersion() >= 1) {
			return parent.getModEntityEntry(world);
		}
		return null;
	}

}

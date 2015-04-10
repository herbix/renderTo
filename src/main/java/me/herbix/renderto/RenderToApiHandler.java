package me.herbix.renderto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.herbix.renderto.api.DefaultRenderToApiRegister;
import me.herbix.renderto.api.IRenderToApiEntry;
import me.herbix.renderto.api.IRenderToApiRegister;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class RenderToApiHandler implements IRenderToApiEntry, IRenderToApiRegister {

	private List<IRenderToApiEntry> apiEntryList = new ArrayList<IRenderToApiEntry>();
	private Map<IRenderToApiEntry, RenderToApiEntryWrapper> wrapperEntryMap = new HashMap<IRenderToApiEntry, RenderToApiEntryWrapper>();

	@Override
	public void register(IRenderToApiEntry entry) {
		RenderToApiEntryWrapper wrapper = new RenderToApiEntryWrapper(entry);
		apiEntryList.add(wrapper);
		wrapperEntryMap.put(entry, wrapper);
	}

	@Override
	public void unregister(IRenderToApiEntry entry) {
		RenderToApiEntryWrapper wrapper = wrapperEntryMap.get(entry);
		if(wrapper != null) {
			apiEntryList.remove(wrapper);
			wrapperEntryMap.remove(entry);
		}
	}

	@Override
	public int getVersion() {
		return DefaultRenderToApiRegister.API_VERSION;
	}

	@Override
	public Map<String, Entity> getModEntityEntry(World world) {
		Map<String, Entity> map = new HashMap<String, Entity>();
		for(IRenderToApiEntry entry : apiEntryList) {
			Map<String, Entity> entrymap = entry.getModEntityEntry(world);
			if(entrymap != null) {
				map.putAll(entrymap);
			}
		}
		return map;
	}

}

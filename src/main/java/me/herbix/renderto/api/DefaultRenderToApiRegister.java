package me.herbix.renderto.api;

public class DefaultRenderToApiRegister {
	
	public static final int API_VERSION = 1;
	public static IRenderToApiRegister instance = null;

	public static void register(IRenderToApiEntry entry) {
		if(instance != null) {
			instance.register(entry);
		}
	}

	public static void unregister(IRenderToApiEntry entry) {
		if(instance != null) {
			instance.unregister(entry);
		}
	}

}

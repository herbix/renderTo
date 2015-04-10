package me.herbix.renderto.api;


public interface IRenderToApiRegister {
	
	public void register(IRenderToApiEntry entry);

	public void unregister(IRenderToApiEntry entry);
	
}

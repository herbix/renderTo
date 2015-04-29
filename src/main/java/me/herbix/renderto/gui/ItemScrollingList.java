package me.herbix.renderto.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.fml.client.GuiScrollingList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class ItemScrollingList extends GuiScrollingList {
	
	private List<?> list;
	private RenderToGuiScreen parent;
	private boolean lastPressed = false;

	public ItemScrollingList(RenderToGuiScreen parent, List<?> model, int width, int height, int top, int bottom, int left) {
		super(parent.mc, width, height, top, bottom, left, 15);
		this.list = model;
		this.parent = parent;
	}

	@Override
	protected int getSize() {
		return list.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		parent.listSelect(this, index);
	}

	@Override
	protected boolean isSelected(int index) {
		return parent.isSelected(this, index);
	}

	@Override
	protected void drawBackground() {
		
	}

	@Override
	protected void drawSlot(int index, int var2, int var3, int var4, Tessellator var5) {
		String showName = list.get(index).toString();
		this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(showName, listWidth - 10), this.left + 3 , var3 + 2, 0xFFFFFF);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float p_22243_3_) {
		int listLength = this.getSize();
        int scrollBarXStart = this.left + this.listWidth - 6;
        int scrollBarXEnd = scrollBarXStart + 6;
        int boxLeft = this.left;
        int boxRight = scrollBarXEnd;

        if(!lastPressed && Mouse.isButtonDown(0) && (mouseX < boxLeft || mouseX > boxRight)) {
        	mouseY = top - 1;
        }
        super.drawScreen(mouseX, mouseY, p_22243_3_);
		if(!Mouse.isButtonDown(0) && lastPressed) {
			try {
				parent.handleMouseInput();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		lastPressed = Mouse.isButtonDown(0);
		
        this.overlayBackground(0, this.top, 255, 255);
        this.overlayBackground(this.bottom, this.listHeight, 255, 255);
	}
	
    private void overlayBackground(int p_22239_1_, int p_22239_2_, int p_22239_3_, int p_22239_4_)
    {
        Tessellator var5 = Tessellator.getInstance();
        WorldRenderer worldr = var5.getWorldRenderer();
        parent.mc.renderEngine.bindTexture(Gui.optionsBackground);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var6 = 32.0F;
        worldr.startDrawingQuads();
        worldr.setColorRGBA_I(4210752, p_22239_4_);
        worldr.addVertexWithUV(0.0D, p_22239_2_, 0.0D, 0.0D, p_22239_2_ / var6);
        worldr.addVertexWithUV((double)this.listWidth + 30, p_22239_2_, 0.0D, (this.listWidth + 30) / var6, p_22239_2_ / var6);
        worldr.setColorRGBA_I(4210752, p_22239_3_);
        worldr.addVertexWithUV((double)this.listWidth + 30, p_22239_1_, 0.0D, (this.listWidth + 30) / var6, p_22239_1_ / var6);
        worldr.addVertexWithUV(0.0D, p_22239_1_, 0.0D, 0.0D, p_22239_1_ / var6);
        var5.draw();
    }

	public int getWidth() {
		return listWidth;
	}
	
	public int getTop() {
		return top;
	}
	
	public int getBottom() {
		return bottom;
	}

}

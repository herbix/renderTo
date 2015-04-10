package me.herbix.renderto.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

public class RenderToGuiScreen extends GuiScreen implements ISlider {
	
	private GuiScreen parent;
	private ItemScrollingList domainList;
	private ItemScrollingList itemList;
	private GuiButton[] switches = new GuiButton[3];
	private GuiTextField sizeOutput;

	private List<String> domainListModel = new ArrayList<String>();
	private List<String> itemListModel = new ArrayList<String>();

	private int domainListSelection = 0;
	private int itemListSelection = 0;
	private int selectedButton = 0;
	
	private RenderSetting globalSetting = new RenderSetting();
	
	private Map<String, Entity> cachedEntities = new HashMap<String, Entity>();

	public RenderToGuiScreen(GuiScreen parent) {
		this.parent = parent;
		initDomains();
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(200, width / 2 - 100, height - 30, I18n.format("gui.done")));
		buttonList.add(switches[0] = new GuiButton(100, width - 160, 10, 50, 20, i18n("item")));
		buttonList.add(switches[1] = new GuiButton(100, width - 110, 10, 50, 20, i18n("block")));
		buttonList.add(switches[2] = new GuiButton(100, width - 60, 10, 50, 20, i18n("entity")));
		switches[selectedButton].enabled = false;

		domainList = new ItemScrollingList(this, domainListModel, (int)(width * 0.35) - 15, (int)(this.height * 0.4) + 5, 10, (int)(this.height * 0.4) - 5, 10);
		itemList = new ItemScrollingList(this, itemListModel, (int)(width * 0.35) - 15, height, (int)(this.height * 0.4) + 5, this.height - 40, 10);

		buttonList.add(new GuiButton(103, (int)(width * 0.35) + 5, height - 60, 30, 20, i18n("left")));
		buttonList.add(new GuiButton(104, (int)(width * 0.35) + 35, height - 60, 30, 20, i18n("right")));

		buttonList.add(new GuiButton(105, width - 130, height - 60, 60, 20, i18n("save")));
		buttonList.add(new GuiButton(106, width - 70, height - 60, 60, 20, i18n("saveall")));
		
		GuiSlider slider = new GuiSlider(102, (int)(width * 0.35) + 5, height - 80, width - (int)(width * 0.35) - 15, 20, "", "x", 0.1, 2, globalSetting.size, true, true, this);
		buttonList.add(slider);
		slider.precision = 2;
		slider.updateSlider();
		
		sizeOutput = new GuiTextField(107, fontRendererObj, (int)(width * 0.35) + 105, height - 59, width - (int)(width * 0.35) - 236, 18);
		sizeOutput.setFocused(true);
		sizeOutput.setText(String.valueOf(globalSetting.outputSize));
		sizeOutput.setMaxStringLength(3);
	}
	
	private void initDomains() {
		domainListModel.add("minecraft");
		List<ModContainer> ml = Loader.instance().getModList();
		for(ModContainer m : ml) {
			domainListModel.add(m.getModId());
		}
		selectDomainList(domainListSelection);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float tickTime) {
		drawDefaultBackground();
		drawRenderBox();
		
		drawCenteredString(this.fontRendererObj, i18n("title"), this.width / 2, 15, 16777215);

		drawString(fontRendererObj, i18n("outputsize"), (int)(width * 0.35) + 75, height - 54, 16777215);
		
		itemList.drawScreen(mouseX, mouseY, tickTime);
		domainList.drawScreen(mouseX, mouseY, tickTime);
		sizeOutput.drawTextBox();
		
		super.drawScreen(mouseX, mouseY, tickTime);
	}

	private void drawRenderBox() {
		int l = (int)(width * 0.35) + 5;
		int r = width - 10;
		int t = 40;
		int b = height - 90;
		int cxm2 = l + r;
		int cym2 = t + b;
		int w = r - l;
		int h = b - t;
		int radius = Math.min(w, h);
		int left = (cxm2-radius)/2;
		int top = (cym2-radius)/2;

		drawRect(left, top, (cxm2+radius)/2, (cym2+radius)/2, 0x80000000);
		
		String domain = domainListModel.get(domainListSelection);
		String selected = itemListModel.get(itemListSelection);
		if(selectedButton == 0) {
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.pushMatrix();
			GlStateManager.translate(left, top, 50);
			GlStateManager.scale(radius/16.0, radius/16.0, radius/16.0);
			GlStateManager.color(1, 1, 1);
			Item item = Item.getItemFromBlock(Blocks.stone_stairs);
			if(itemListSelection > 0) {
				String name = domain + ":" + selected;
				Item newItem = GameData.getItemRegistry().getObject(name);
				if(newItem != null) {
					item = newItem;
				}
			}
			mc.getRenderItem().renderItemIntoGUI(new ItemStack(item), 0, 0);
			GlStateManager.popMatrix();
			RenderHelper.disableStandardItemLighting();
		} else if(selectedButton == 2) {
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.pushMatrix();
			GlStateManager.color(1, 1, 1);
			float f = (float)(globalSetting.size * 32);
			GlStateManager.translate(cxm2 / 2, cym2 / 2, 50);
			GlStateManager.scale(f, f, f);
			GlStateManager.rotate(150, 1, 0, 0);
			GlStateManager.rotate(225 + globalSetting.rotation * 90, 0, 1, 0);
			GlStateManager.scale(-1, 1, 1);
			Entity entity = cachedEntities.get(domain.equals("minecraft") ? selected : (domain + "." + selected));
			if(entity != null) {
				GlStateManager.translate(0, -entity.height / 2, 0);
				((Render)mc.getRenderManager().entityRenderMap.get(entity.getClass())).doRender(entity, 0, 0, 0, 0, 0);
			}
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
	}
	
	@Override
	public void drawDefaultBackground() {
		drawBackground(0);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch(button.id) {
		case 100:
			for(int i=0; i<switches.length; i++) {
				switches[i].enabled = true;
				if(switches[i] == button) {
					selectedButton = i;
				}
			}
			button.enabled = false;
			selectDomainList(domainListSelection);
			break;
		case 103:
			globalSetting.rotation = (globalSetting.rotation + 1) % 4;
			break;
		case 104:
			globalSetting.rotation = (globalSetting.rotation + 3) % 4;
			break;
		case 200:
			mc.displayGuiScreen(parent);
			break;
		}
	}
	
	public void listSelect(ItemScrollingList list, int index) {
		if(list == itemList) {
			selectItemList(index);
		} else {
			selectDomainList(index);
		}
	}

	private void selectDomainList(int index) {
		domainListSelection = index;
		itemListModel.clear();
		switch(selectedButton) {
		case 0:
			setupItemList();
			break;
		case 1:
			setupBlockList();
			break;
		case 2:
			setupEntityList();
			break;
		}
		Collections.sort(itemListModel);
		itemListModel.add(0, "global setting");
		selectItemList(0);
	}

	private void setupEntityList() {
		String domain = domainListModel.get(domainListSelection) + ".";
		WorldClient theWorld = mc.theWorld;

		for(Object e : EntityList.classToStringMapping.entrySet()) {
			Entry<?, ?> entry = (Entry<?, ?>)e;
			if(!EntityLiving.class.isAssignableFrom(((Class<? extends Entity>)entry.getKey()))) {
				continue;
			}
			String name = entry.getValue().toString();
			int index = name.indexOf('.');
			if((index < 0 && domain.equals("minecraft.")) || name.startsWith(domain)) {
				itemListModel.add(index < 0 ? name : name.substring(index + 1));
				Entity entity;
				try {
					entity = ((Class<? extends Entity>)entry.getKey()).getConstructor(World.class).newInstance(theWorld);
				} catch(Exception ex) {
					entity = new EntityZombie(theWorld);
				}
				
				cachedEntities.put(name, entity);
			}
		}
		
		cachedEntities.put("global setting", new EntityZombie(theWorld));
	}

	private void setupBlockList() {
		String domain = domainListModel.get(domainListSelection) + ":";
		FMLControlledNamespacedRegistry<Block> r1 = GameData.getBlockRegistry();
		for(Object keyobj : r1.getKeys()) {
			String name = keyobj.toString();
			int index = name.indexOf(':');
			if((index < 0 && domain.equals("minecraft:")) || name.startsWith(domain)) {
				itemListModel.add(index < 0 ? name : name.substring(index + 1));
			}
		}
	}

	private void selectItemList(int index) {
		itemListSelection = index;
	}

	private void setupItemList() {
		String domain = domainListModel.get(domainListSelection) + ":";
		FMLControlledNamespacedRegistry<Item> r1 = GameData.getItemRegistry();
		for(Object keyobj : r1.getKeys()) {
			String name = keyobj.toString();
			int index = name.indexOf(':');
			if((index < 0 && domain.equals("minecraft:")) || name.startsWith(domain)) {
				itemListModel.add(index < 0 ? name : name.substring(index + 1));
			}
		}
	}

	private String i18n(String msg, Object ... o) {
		return I18n.format("gui.renderto." + msg, o);
	}

	FontRenderer getFontRenderer() {
		return fontRendererObj;
	}

	public boolean isSelected(ItemScrollingList list, int index) {
		if(list == itemList) {
			return itemListSelection == index;
		} else {
			return domainListSelection == index;
		}
	}

	@Override
	public void onChangeSliderValue(GuiSlider slider) {
		switch(slider.id) {
		case 102:
			globalSetting.size = slider.getValue();
			break;
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(((typedChar >= '0' && typedChar <= '9') || keyCode == 14) && sizeOutput.isFocused()) {
			sizeOutput.textboxKeyTyped(typedChar, keyCode);
			try {
				globalSetting.outputSize = Integer.parseInt(sizeOutput.getText());
			} catch(NumberFormatException e) {
				globalSetting.outputSize = 150;
			}
		}
	}
	
	/*
	Framebuffer fb = null;
	if(!saved) {
		Framebuffer mb = mc.getFramebuffer();
		fb = new Framebuffer(mb.framebufferWidth, mb.framebufferHeight, true);
		fb.framebufferClear();
		fb.bindFramebuffer(false);
	}
	
	RenderHelper.enableGUIStandardItemLighting();
	GlStateManager.pushMatrix();
	float f = (1 + scale * 0.1f) * 16;
	GlStateManager.translate(100, 100, 50);
	GlStateManager.scale(f, f, 0.1);
	GlStateManager.rotate(150, 1, 0, 0);
	GlStateManager.rotate(225, 0, 1, 0);
	GlStateManager.scale(-1, 1, 1);
	Entity entity = new EntityCow(mc.theWorld);
	((Render)mc.getRenderManager().entityRenderMap.get(entity.getClass())).doRender(entity, 0, 0, 0, 0, 0);
	
	//mc.getRenderItem().renderItemIntoGUI(new ItemStack(Blocks.cobblestone), 0, 0);

	RenderHelper.disableStandardItemLighting();
	GlStateManager.popMatrix();

	if(!saved) {
		saveScreenshot(mc.mcDataDir, null, 300, 300, fb);
		mc.getFramebuffer().bindFramebuffer(true);
		fb.deleteFramebuffer();
		saved = true;
	}

	
	private static final Logger logger = LogManager.getLogger();
    private static IntBuffer pixelBuffer; 
    private static int[] pixelValues;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    
	public static IChatComponent saveScreenshot(File gameDirectory, String screenshotName, int width, int height, Framebuffer buffer)
    {
        try
        {
            File file2 = new File(gameDirectory, "screenshots");
            file2.mkdir();

            if (OpenGlHelper.isFramebufferEnabled())
            {
                width = buffer.framebufferTextureWidth;
                height = buffer.framebufferTextureHeight;
            }

            int k = width * height;

            if (pixelBuffer == null || pixelBuffer.capacity() < k)
            {
                pixelBuffer = BufferUtils.createIntBuffer(k);
                pixelValues = new int[k];
            }

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();

            if (OpenGlHelper.isFramebufferEnabled())
            {
                GlStateManager.bindTexture(buffer.framebufferTexture);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            }
            else
            {
                GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            }

            pixelBuffer.get(pixelValues);
            TextureUtil.processPixelValues(pixelValues, width, height);
            BufferedImage bufferedimage = null;

            if (OpenGlHelper.isFramebufferEnabled())
            {
                bufferedimage = new BufferedImage(buffer.framebufferWidth, buffer.framebufferHeight, BufferedImage.TYPE_INT_ARGB);
                int l = buffer.framebufferTextureHeight - buffer.framebufferHeight;

                for (int i1 = l; i1 < buffer.framebufferTextureHeight; ++i1)
                {
                    for (int j1 = 0; j1 < buffer.framebufferWidth; ++j1)
                    {
                        bufferedimage.setRGB(j1, i1 - l, pixelValues[i1 * buffer.framebufferTextureWidth + j1]);
                    }
                }
            }
            else
            {
                bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                bufferedimage.setRGB(0, 0, width, height, pixelValues, 0, width);
            }

            File file3;

            if (screenshotName == null)
            {
                file3 = getTimestampedPNGFileForDirectory(file2);
            }
            else
            {
                file3 = new File(file2, screenshotName);
            }

            ImageIO.write(bufferedimage, "png", file3);
            ChatComponentText chatcomponenttext = new ChatComponentText(file3.getName());
            chatcomponenttext.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath()));
            chatcomponenttext.getChatStyle().setUnderlined(Boolean.valueOf(true));
            return new ChatComponentTranslation("screenshot.success", new Object[] {chatcomponenttext});
        }
        catch (Exception exception)
        {
            logger.warn("Couldn\'t save screenshot", exception);
            return new ChatComponentTranslation("screenshot.failure", new Object[] {exception.getMessage()});
        }
    }

    private static File getTimestampedPNGFileForDirectory(File gameDirectory)
    {
        String s = dateFormat.format(new Date()).toString();
        int i = 1;

        while (true)
        {
            File file2 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");

            if (!file2.exists())
            {
                return file2;
            }

            ++i;
        }
    }*/
}

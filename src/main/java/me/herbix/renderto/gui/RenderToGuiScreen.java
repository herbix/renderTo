package me.herbix.renderto.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import me.herbix.renderto.FakeItem;
import me.herbix.renderto.RenderToMod;
import me.herbix.renderto.util.AnimatedGifEncoder;
import me.herbix.renderto.util.FrameBufferUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

import com.google.common.collect.ImmutableSet;

public class RenderToGuiScreen extends GuiScreen implements ISlider {
	
	private GuiScreen parent;
	private GuiSlider rotationSlider;
	private ItemScrollingList domainList;
	private ItemScrollingList itemList;
	private GuiButton[] switches = new GuiButton[3];
	private GuiTextField sizeOutput;
	private GuiTextField filter;
	private GuiCheckBox saveGif;

	private List<String> domainListModel = new ArrayList<String>();
	private List<String> itemListModel = new ArrayList<String>();

	private int domainListSelection = 0;
	private int itemListSelection = 0;
	private int selectedButton = 0;
	
	private int sliderXPos = 0;
	
	private RenderSetting globalSetting = new RenderSetting();
	
	private Map<String, ItemStack> cachedItems = new HashMap<String, ItemStack>();
	private Map<String, Entity> cachedEntities = new HashMap<String, Entity>();
	private Map<String, IBlockState> cachedBlocks = new HashMap<String, IBlockState>();
	
	private String currentFilter = "";
	private String currentFilterPattern = "(.*)";

	private File saveDir;
	
	private FakeItem fakeItem = new FakeItem();

	public RenderToGuiScreen(GuiScreen parent) {
		this.parent = parent;
		initDomains();
	}

	@Override
	public void initGui() {
		super.initGui();

		domainList = new ItemScrollingList(this, domainListModel, (int)(width * 0.35) - 15, (int)(this.height * 0.4) + 5, 10, (int)(this.height * 0.4) - 25, 10);
		itemList = new ItemScrollingList(this, itemListModel, (int)(width * 0.35) - 15, height, (int)(this.height * 0.4) + 5, this.height - 40, 10);

		initButtonList();
		
		saveDir = new File(mc.mcDataDir, "/RenderTo");
	}

	private void initButtonList() {
		buttonList.clear();
		buttonList.add(new GuiButton(200, 11, height - 30, (int)(width * 0.35) - 17, 20, I18n.format("gui.done")));
		buttonList.add(switches[0] = new GuiButton(100, width - 160, 10, 50, 20, i18n("item")));
		buttonList.add(switches[1] = new GuiButton(100, width - 110, 10, 50, 20, i18n("block")));
		buttonList.add(switches[2] = new GuiButton(100, width - 60, 10, 50, 20, i18n("entity")));
		switches[selectedButton].enabled = false;

		sliderXPos = (int) (width * 0.35) + 5;
		
		int startpos = sliderXPos;
		int endpos = width - 10;
		int quarterpos = (3 * startpos + endpos) / 4;
		int halfpos = (startpos + endpos) / 2;
		int threequarterpos = (startpos + 3 * endpos) / 4;
		
		if((startpos - endpos) % 2 != 0) startpos++;
		if((halfpos - startpos) % 2 != 0) halfpos++;
		if((endpos - threequarterpos) % 2 != 0) threequarterpos++;
		
		if(selectedButton == 2) {
			GuiSlider slider = new GuiSlider(102, startpos, height - 70, endpos - startpos, 20, "", "x", 0.1, 2, globalSetting.size, true, true, this);
			buttonList.add(slider);
			slider.precision = 2;
			slider.updateSlider();

			saveGif = new GuiCheckBox(109, startpos + 5, height - 25, i18n("savegif"), globalSetting.saveGif);
			buttonList.add(saveGif);
		}

		if(selectedButton != 0) {
			rotationSlider = new GuiSlider(103, startpos, height - 50, halfpos - startpos, 20, i18n("rotation.prefix"), i18n("rotation.suffix"), 0, 360, globalSetting.rotation, true, true, this);
			buttonList.add(rotationSlider);
			rotationSlider.precision = 0;
			rotationSlider.updateSlider();
			
			buttonList.add(new GuiButton(104, halfpos, height - 50, endpos - halfpos, 20, globalSetting.lockRotation ? i18n("lockyes") : i18n("lockno")));
		}
		
		buttonList.add(new GuiButton(105, halfpos, height - 30, threequarterpos - halfpos, 20, i18n("save")));
		buttonList.add(new GuiButton(106, threequarterpos, height - 30, endpos - threequarterpos, 20, i18n("saveall")));

		sizeOutput = new GuiTextField(107, fontRendererObj, quarterpos + 1, height - 29, halfpos - quarterpos - 2, 18);
		sizeOutput.setText(String.valueOf(globalSetting.outputSize));
		sizeOutput.setMaxStringLength(3);

		filter = new GuiTextField(108, fontRendererObj, 11, (int)(this.height * 0.4) - 15, (int)(width * 0.35) - 17, 18);
		filter.setText(currentFilter);
		filter.setMaxStringLength(128);
	}
	
	private void initDomains() {
		domainListModel.add("minecraft");
		List<ModContainer> ml = Loader.instance().getModList();
		ImmutableSet<String> excludeMods = ImmutableSet.of("mcp", "Forge", "FML");
		for(ModContainer m : ml) {
			if(excludeMods.contains(m.getModId())) {
				continue;
			}
			domainListModel.add(m.getModId());
		}
		selectDomainList(domainListSelection);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float tickTime) {
		drawDefaultBackground();
		drawRenderBox();
		
		drawCenteredString(fontRendererObj, i18n("title"), this.width / 2, 15, 16777215);

		itemList.drawScreen(mouseX, mouseY, tickTime);
		domainList.drawScreen(mouseX, mouseY, tickTime);
		filter.drawTextBox();

		if(selectedButton != 2) {
			drawString(fontRendererObj, i18n("outputsize"), sliderXPos + 3, height - 23, 16777215);
			sizeOutput.drawTextBox();
		}
		
		super.drawScreen(mouseX, mouseY, tickTime);

		if(mouseY >= itemList.getTop() && mouseY < itemList.getBottom()) {
			int index = itemList.func_27256_c(mouseX, mouseY);
			if(index >= 0 && index < itemListModel.size()) {
				String showName = itemListModel.get(index);
				if(fontRendererObj.getStringWidth(showName) + 6 > itemList.getWidth()) {
					drawHoveringText(Arrays.asList(showName.trim().substring(0, showName.length() - 1).split("[\\[,]{1}")), mouseX, mouseY);
				}
			}
		}

		if(selectedButton == 2 && mouseX >= saveGif.xPosition &&
				mouseX <= saveGif.xPosition + saveGif.width &&
				mouseY >= saveGif.yPosition && mouseY <= saveGif.yPosition + saveGif.height) {
			drawHoveringText(Arrays.asList(i18n("savegif.warning").split(";")), mouseX, mouseY);
		}
	}

	private void drawRenderBox() {
		int l = (int)(width * 0.35) + 5;
		int r = width - 10;
		int t = 40;
		int b = selectedButton == 2 ? (height - 80) : (height - 60);
		int cxm2 = l + r;
		int cym2 = t + b;
		int w = r - l;
		int h = b - t;
		int radius = Math.min(w, h);
		int left = (cxm2-radius)/2;
		int top = (cym2-radius)/2;

		drawRect(left, top, (cxm2+radius)/2, (cym2+radius)/2, 0x80000000);
		
		if(domainListModel.isEmpty() || itemListModel.isEmpty()) {
			return;
		}
		
		String domain = domainListModel.get(domainListSelection);
		String selected = itemListModel.get(itemListSelection);
		if(selectedButton == 0) {
			drawItem(radius, left, top, selected);
		} else if(selectedButton == 1) {
			drawBlock(radius, left, top, selected);
		} else if(selectedButton == 2) {
			drawEntity(cxm2, cym2, domain, selected);
		}
	}

	private void drawBlock(int radius, int left, int top, String selected) {
		setupRenderItemGlState(radius, left, top);
		drawBlockCore(selected);
		releaseRenderItemGlState();
	}

	private void drawEntity(int cxm2, int cym2, String domain, String selected) {
		Entity entity = cachedEntities.get(domain.equals("minecraft") ? selected : (domain + "." + selected));
		AxisAlignedBB bb = entity.getEntityBoundingBox();
		float f = (float)(globalSetting.size * 32);
		setupRenderEntityGlState(cxm2, cym2, entity, f);
		drawEntityCore(entity, bb);
		releaseRenderEntityGlState();
	}

	private void setupRenderEntityGlState(int cxm2, int cym2, Entity entity, float f) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1);
		GlStateManager.translate(cxm2 / 2, cym2 / 2, 0);
		GlStateManager.scale(f, f, f);
		GlStateManager.translate(0, 0, 2.5 * (entity.height / 4 + entity.width / 2 * 0.866));
		RenderHelper.enableGUIStandardItemLighting();
	}

	private void releaseRenderEntityGlState() {
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
	}

	private void drawItem(int radius, int left, int top, String selected) {
		setupRenderItemGlState(radius, left, top);
		drawItemCore(selected);
		releaseRenderItemGlState();
	}

	private void setupRenderItemGlState(int radius, int left, int top) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(left, top, 50);
		GlStateManager.scale(radius / 16.0, radius / 16.0, radius / 16.0);
		GlStateManager.translate(0, 0, -radius / 4);
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.color(1, 1, 1);
	}

	private void releaseRenderItemGlState() {
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
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
			initButtonList();
			selectDomainList(domainListSelection);
			break;
		case 104:
			globalSetting.lockRotation = !globalSetting.lockRotation;
			button.displayString = globalSetting.lockRotation ? i18n("lockyes") : i18n("lockno");
			if (globalSetting.lockRotation) {
				double curValue = rotationSlider.getValue();
				double newValue = 0;
				int temp = (int) (curValue % 90);
				if (temp < 45) {
					newValue = curValue - temp;
				} else {
					newValue = curValue + 90 - temp;
				}
				int newValueInt = MathHelper.floor_double(newValue);
				globalSetting.rotation = newValueInt;
				rotationSlider.displayString = String.valueOf(newValueInt) + " Rotation";
				rotationSlider.setValue(newValueInt);
			}
			break;
		case 105:
			saveCurrentSelection();
			break;
		case 106:
			saveAll();
			break;
		case 109:
			globalSetting.saveGif = ((GuiCheckBox)button).isChecked();
			break;
		case 200:
			mc.displayGuiScreen(parent);
			break;
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		filter.mouseClicked(mouseX, mouseY, mouseButton);
		sizeOutput.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	private void saveAll() {
		String domain = domainListModel.get(domainListSelection);
		for(String selected : itemListModel) {
			if(selectedButton == 0) {
				saveItemPic(domain, selected, globalSetting.outputSize);
			} else if(selectedButton == 1) {
				saveBlockPic(domain, selected, globalSetting.outputSize);
			} else if(selectedButton == 2) {
				saveEntityPic(domain, selected, globalSetting.size);
			}
		}
	}

	private void saveCurrentSelection() {
		String domain = domainListModel.get(domainListSelection);
		String selected = itemListModel.get(itemListSelection);
		if(selectedButton == 0) {
			saveItemPic(domain, selected, globalSetting.outputSize);
		} else if(selectedButton == 1) {
			saveBlockPic(domain, selected, globalSetting.outputSize);
		} else if(selectedButton == 2) {
			saveEntityPic(domain, selected, globalSetting.size);
		}
	}

	private void saveBlockPic(String domain, String selected, int outputSize) {
		File f = new File(saveDir, domain + "/block/" + outputSize + "x/" + selected.replaceAll("[^0-9a-zA-Z]{1}", "_") + ".png");
		f.getParentFile().mkdirs();

		Framebuffer mb = mc.getFramebuffer();
		int radius = Math.min(mb.framebufferWidth, mb.framebufferHeight);
		Framebuffer fb = new Framebuffer(mb.framebufferWidth * outputSize / radius, mb.framebufferHeight * outputSize / radius, true);
		fb.framebufferClear();
		fb.bindFramebuffer(false);
		
		int r = radius * this.width / mb.framebufferWidth;

		setupRenderItemGlState(r, 0, 0);
		drawBlockCore(selected);
		releaseRenderItemGlState();
		
		FrameBufferUtils.instance.saveFrameBufferToFile(fb, f, outputSize, outputSize, false);
		
		mb.bindFramebuffer(true);
		fb.deleteFramebuffer();
	}

	private void drawBlockCore(String selected) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		IBlockState state = cachedBlocks.get(selected);
		IBakedModel ibakedmodel = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		GlStateManager.pushMatrix();
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		GlStateManager.translate(0, 0, 100.0F + mc.getRenderItem().zLevel);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, 1.0F, -1.0F);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		
		if (ibakedmodel.isGui3d()) {
		    GlStateManager.scale(40.0F, 40.0F, 40.0F);
		    GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
		    GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		    GlStateManager.enableLighting();
		} else {
		    GlStateManager.scale(64.0F, 64.0F, 64.0F);
		    GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
		    GlStateManager.disableLighting();
		}
		
		GlStateManager.rotate(globalSetting.rotation, 0, 1, 0);
		
		fakeItem.setWorld(mc.theWorld);
		fakeItem.setForBlock(state);
		mc.getRenderItem().renderItem(new ItemStack(fakeItem), ibakedmodel);

		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
	}

	private void saveItemPic(String domain, String selected, int outputSize) {
		File f = new File(saveDir, domain + "/item/" + outputSize + "x/" + selected.replaceAll("[^0-9a-zA-Z]{1}", "_") + ".png");
		f.getParentFile().mkdirs();

		Framebuffer mb = mc.getFramebuffer();
		int radius = Math.min(mb.framebufferWidth, mb.framebufferHeight);
		Framebuffer fb = new Framebuffer(mb.framebufferWidth * outputSize / radius, mb.framebufferHeight * outputSize / radius, true);
		fb.framebufferClear();
		fb.bindFramebuffer(false);
		
		int r = radius * this.width / mb.framebufferWidth;

		setupRenderItemGlState(r, 0, 0);
		drawItemCore(selected);
		releaseRenderItemGlState();

		FrameBufferUtils.instance.saveFrameBufferToFile(fb, f, outputSize, outputSize, false);
		
		mb.bindFramebuffer(true);
		fb.deleteFramebuffer();
	}

	private void drawItemCore(String selected) {
		ItemStack stack = cachedItems.get(selected);
		mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
	}

	private void saveEntityPic(String domain, String selected, double size) {
		String fileSuffix = globalSetting.saveGif ? ".gif" : ".png";
		File f = new File(saveDir, domain + "/entity/" + size + "x/" + selected.replaceAll("[\\\\\\/\\?\\:\\>\\<\\|\\*\\\"]{1}", "_") + fileSuffix);
		f.getParentFile().mkdirs();

		Entity entity = cachedEntities.get(domain.equals("minecraft") ? selected : (domain + "." + selected));
		if(entity == null) {
			return;
		}
		double sizex = 0.5 * size * entity.width * 1.414;
		double sizey = 0.5 * size * (entity.height * 0.866 + entity.width * 0.707);
		int outx = (int)(64 * sizex + 0.5);
		int outy = (int)(64 * sizey + 0.5);
		int result;
		do {
			Framebuffer mb = mc.getFramebuffer();
			if(globalSetting.saveGif) {
				result = saveEntityGif(size, f, entity, outx, outy, mb);
			} else {
				Framebuffer fb = createAndRenderEntityToFrameBuffer(size, entity, outx, outy, mb);
				result = FrameBufferUtils.instance.saveFrameBufferToFile(fb, f, outx, outy, true);
				fb.deleteFramebuffer();
			}

			if ((result & 1) != 0) {
				outx *= 2;
			}
			if ((result & 2) != 0) {
				outy *= 2;
			}
			mb.bindFramebuffer(true);
		} while(result != 0);
	}

	private int saveEntityGif(double size, File f, Entity entity, int outx, int outy, Framebuffer mb) {
		int result = 0;
		int oldRotation = globalSetting.rotation;

		final int count = 360;
		BufferedImage[] images = new BufferedImage[count];
		Rectangle[] bounds = new Rectangle[count];
		int cachedImageSize = 0;
		boolean cacheImages = true;

		Rectangle allBound = null;

		for(int i=0; i<count; i++) {
			globalSetting.rotation = 360 * i / count;
			Framebuffer fb = createAndRenderEntityToFrameBuffer(size, entity, outx, outy, mb);
			BufferedImage img = FrameBufferUtils.instance.getImageFromFrameBuffer(fb, outx, outy);
			Rectangle bound = FrameBufferUtils.instance.getImageBound(img);
			result = FrameBufferUtils.instance.compareImage(img, bound);

			if(result != 0) {
				return result;
			}
			allBound = (allBound == null) ? bound : allBound.union(bound);

			if(cacheImages) {
				bounds[i] = bound;
				images[i] = FrameBufferUtils.instance.cutPicture(img, bound);
				cachedImageSize += 4 * (bound.width * bound.height);
				if(cachedImageSize > 300 * 1024 * 1024) {
					cacheImages = false;
					bounds[i] = null;
					images[i] = null;
				}
			}

			fb.deleteFramebuffer();
		}

		AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.start(f.getAbsolutePath());
		encoder.setDelay(30);
		encoder.setRepeat(0);

		BufferedImage bg = new BufferedImage(allBound.width, allBound.height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = bg.createGraphics();

		if(cacheImages) {
			for (int i = 0; i < count; i++) {
				g.setColor(Color.GREEN);
				g.fillRect(0, 0, bg.getWidth(), bg.getHeight());
				g.drawImage(images[i], bounds[i].x - allBound.x, bounds[i].y - allBound.y, null);
				encoder.setTransparent(Color.green);
				encoder.addFrame(bg);
			}
		} else {
			for (int i = 0; i < count; i++) {
				globalSetting.rotation = 360 * i / count;
				Framebuffer fb = createAndRenderEntityToFrameBuffer(size, entity, outx, outy, mb);
				BufferedImage img = FrameBufferUtils.instance.getImageFromFrameBuffer(fb, outx, outy);
				img = FrameBufferUtils.instance.cutPicture(img, allBound);
				g.setColor(Color.GREEN);
				g.fillRect(0, 0, bg.getWidth(), bg.getHeight());
				g.drawImage(img, 0, 0, null);
				encoder.setTransparent(Color.green);
				encoder.addFrame(bg);
				fb.deleteFramebuffer();
			}
		}

		g.dispose();

		encoder.finish();
		globalSetting.rotation = oldRotation;
		return result;
	}

	private Framebuffer createAndRenderEntityToFrameBuffer(double size, Entity entity, int outx, int outy, Framebuffer mb) {
		int radius = Math.min(mb.framebufferWidth * outy, mb.framebufferHeight * outx);
		Framebuffer fb = new Framebuffer(mb.framebufferWidth * outx * outy / radius, mb.framebufferHeight * outx * outy / radius, true);
		fb.framebufferClear();
		fb.bindFramebuffer(false);

		int r = radius * this.width / mb.framebufferWidth;
		float scale = (float)(size * 64) * r / outx / outy;
		AxisAlignedBB bb = entity.getEntityBoundingBox();

		setupRenderEntityGlState(r / outy, r / outx, entity, scale);
		drawEntityCore(entity, bb);
		releaseRenderEntityGlState();
		return fb;
	}

	private void drawEntityCore(Entity entity, AxisAlignedBB bb) {
		GlStateManager.rotate(150, 1, 0, 0);
		GlStateManager.rotate(225 + globalSetting.rotation, 0, 1, 0);
		GlStateManager.scale(-1, 1, 1);
		if(entity != null) {
			GlStateManager.translate(0, (bb.minY - bb.maxY) / 2, 0);
			((Render)mc.getRenderManager().entityRenderMap.get(entity.getClass())).doRender(entity, 0, 0, 0, 0, 0);
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
		selectItemList(0);
	}

	private void setupEntityList() {
		String domain = domainListModel.get(domainListSelection) + ".";
		WorldClient theWorld = mc.theWorld;
		
		cachedEntities.clear();

		for(Object e : EntityList.classToStringMapping.entrySet()) {
			Entry<?, ?> entry = (Entry<?, ?>)e;
			if(!EntityLiving.class.isAssignableFrom(((Class<? extends Entity>)entry.getKey()))) {
				continue;
			}
			if(Modifier.isAbstract(((Class<? extends Entity>)entry.getKey()).getModifiers())) {
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
		
		Map<String, Entity> map = RenderToMod.api.getModEntityEntry(theWorld);
		for(Entry<String, Entity> e : map.entrySet()) {
			String name = (String)EntityList.classToStringMapping.get(e.getValue().getClass());
			if(name != null) {
				int index = name.indexOf('.');
				if((index < 0 && domain.equals("minecraft.")) || name.startsWith(domain)) {
					itemListModel.remove(index < 0 ? name : name.substring(index + 1));
					itemListModel.add(e.getKey());
					cachedEntities.put((index < 0 ? "" : domain) + e.getKey(), e.getValue());
				}
			}
		}
		
		for(Iterator<String> it = itemListModel.iterator(); it.hasNext(); ) {
			String name = it.next();
			if(!checkMatches(name, currentFilterPattern)) {
				it.remove();
				cachedEntities.remove(name);
			}
		}

		cachedEntities.put("global setting", new EntityZombie(theWorld));
	}

	private void setupBlockList() {
		String domain = domainListModel.get(domainListSelection) + ":";
		FMLControlledNamespacedRegistry<Block> r1 = GameData.getBlockRegistry();
		
		cachedBlocks.clear();
		for(Object keyobj : r1.getKeys()) {
			String name = keyobj.toString();
			int index = name.indexOf(':');
			if((index < 0 && domain.equals("minecraft:")) || name.startsWith(domain)) {
				String blockName = index < 0 ? name : name.substring(index + 1);
				Block block = r1.getObject(name);
				List<IBlockState> states = block.getBlockState().getValidStates();
				if(states.size() == 1) {
					if(checkMatches(blockName, currentFilterPattern)) {
						itemListModel.add(blockName);
						cachedBlocks.put(blockName, block.getDefaultState());
					}
				} else {
					for(IBlockState state : states) {
						String stateString = state.toString();
						int indexOf = stateString.indexOf(':');
						String stateName = indexOf < 0 ? stateString : stateString.substring(indexOf + 1);
						if(checkMatches(stateName, currentFilterPattern)) {
							itemListModel.add(stateName);
							cachedBlocks.put(stateName, state);
						}
					}
				}
			}
		}
	}

	private void selectItemList(int index) {
		itemListSelection = index;
	}

	private void setupItemList() {
		String domain = domainListModel.get(domainListSelection) + ":";
		FMLControlledNamespacedRegistry<Item> r1 = GameData.getItemRegistry();
		cachedItems.clear();
		for(Object keyobj : r1.getKeys()) {
			String name = keyobj.toString();
			int index = name.indexOf(':');
			if((index < 0 && domain.equals("minecraft:")) || name.startsWith(domain)) {
				String fillName = index < 0 ? name : name.substring(index + 1);
				Item item = r1.getObject(name);
				if(item.getHasSubtypes()) {
					List<ItemStack> itemStacks = new ArrayList<ItemStack>();
					item.getSubItems(item, item.getCreativeTab(), itemStacks);
					int id = 0;
					for(ItemStack stack : itemStacks) {
						String indexName = String.format("%s:%03d", fillName, id++);
						if(checkMatches(indexName, currentFilterPattern)) {
							itemListModel.add(indexName);
							cachedItems.put(indexName, stack);
						}
					}
				} else {
					if(checkMatches(fillName, currentFilterPattern)) {
						itemListModel.add(fillName);
						cachedItems.put(fillName, new ItemStack(r1.getObject(name)));
					}
				}
			}
		}
	}
	
	private Pattern savedPattern;
	private String oldPatternString;
	
	private boolean checkMatches(String string, String pattern) {
		if(oldPatternString == pattern) {
			return savedPattern.matcher(string).matches();
		}
		try {
			savedPattern = Pattern.compile(pattern);
			oldPatternString = pattern;
			return savedPattern.matcher(string).matches();
		} catch(Exception e) {
			return false;
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
			globalSetting.size = MathHelper.floor_double(100 * slider.getValue()) / 100.0;
			break;
		case 103:
			if (globalSetting.lockRotation) {
				double curValue = MathHelper.floor_double(100 * slider.getValue()) / 100.0;
				double newValue = 0;
				int temp = (int) (curValue % 90);
				if (temp < 45) {
					newValue = curValue - temp;
				} else {
					newValue = curValue + 90 - temp;
				}
				newValue = Math.floor(newValue);
				globalSetting.rotation = (int) newValue;
				slider.displayString = String.valueOf(newValue) + " Rotation";
				slider.setValue(newValue);
			} else {
				globalSetting.rotation = (int) (MathHelper.floor_double(100 * slider.getValue()) / 100.0);
			}
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
			if(globalSetting.outputSize <= 0) {
				globalSetting.outputSize = 1;
			}
		} else if(filter.isFocused()) {
			filter.textboxKeyTyped(typedChar, keyCode);
			currentFilter = filter.getText();
			if(currentFilter.length() == 0) {
				currentFilterPattern = "(.*)";
			} else {
				currentFilterPattern = "(.*)" + currentFilter + "(.*)";
			}
			selectDomainList(domainListSelection);
		}
	}

}

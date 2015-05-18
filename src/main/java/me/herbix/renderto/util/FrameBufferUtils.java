package me.herbix.renderto.util;

import me.herbix.renderto.RenderToMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.util.Arrays;

public class FrameBufferUtils {

	public static final FrameBufferUtils instance = new FrameBufferUtils();

	private FrameBufferUtils() {}

	private IntBuffer pixelBuffer;
	private int[] pixelValues;

	public BufferedImage cutPicture(BufferedImage image, Rectangle bound) {
		BufferedImage image2 = new BufferedImage(bound.width, bound.height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image2.createGraphics();
		g.drawImage(image, -bound.x, -bound.y, null);
		g.dispose();
		return image2;
	}

	public int compareImage(BufferedImage image, Rectangle bound) {
		int r = 0;
		if(bound.x == 0 || bound.x + bound.width == image.getWidth()) {
			r |= 1;
		}
		if(bound.y == 0 || bound.y + bound.height == image.getHeight()) {
			r |= 2;
		}
		return r;
	}

	public Rectangle getImageBound(BufferedImage image) {
		int h = image.getHeight();
		int w = image.getWidth();
		if(pixelValues.length < h * w) {
			pixelValues = new int[h * w];
		}

		image.getRGB(0, 0, w, h, pixelValues, 0, w);

		int t, l, b, r;
		o:for(t=0; t<h; t++) {
			for(int i=0; i<w; i++) {
				if((pixelValues[t*w+i] & 0xFF000000) != 0) {
					break o;
				}
			}
		}
		o:for(b=h; b>0; b--) {
			for(int i=0; i<w; i++) {
				if((pixelValues[(b-1)*w+i] & 0xFF000000) != 0) {
					break o;
				}
			}
		}
		o:for(l=0; l<w; l++) {
			for(int i=0; i<h; i++) {
				if((pixelValues[i*w+l] & 0xFF000000) != 0) {
					break o;
				}
			}
		}
		o:for(r=w; r>0; r--) {
			for(int i=0; i<h; i++) {
				if((pixelValues[i*w+(r-1)] & 0xFF000000) != 0) {
					break o;
				}
			}
		}

		return new Rectangle(l, t, r-l, b-t);
	}

	public int saveFrameBufferToFile(Framebuffer buffer, File f, int width, int height, boolean cut) {
		try {
			BufferedImage bufferedimage = getImageFromFrameBuffer(buffer, width, height);
			if(cut) {
				Rectangle bound = getImageBound(bufferedimage);
				int compareResult = compareImage(bufferedimage, bound);
				if(compareResult == 0) {
					bufferedimage = cutPicture(bufferedimage, bound);
				} else {
					return compareResult;
				}
			}
			ImageIO.write(bufferedimage, "png", f);
		} catch(Exception e) {
			RenderToMod.logger.error("Cannot save framebuffer to file " + f);
			RenderToMod.logger.error(e + ": " + e.getMessage());
			RenderToMod.logger.error(Arrays.toString(e.getStackTrace()));
		}
		return 0;
	}

	public BufferedImage getImageFromFrameBuffer(Framebuffer buffer, int width, int height) {
		int k = buffer.framebufferTextureWidth * buffer.framebufferTextureHeight;
		if (pixelBuffer == null || pixelBuffer.capacity() < k) {
			pixelBuffer = BufferUtils.createIntBuffer(k);
			pixelValues = new int[k];
		}
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		pixelBuffer.clear();
		GlStateManager.bindTexture(buffer.framebufferTexture);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
		pixelBuffer.get(pixelValues);
		TextureUtil.processPixelValues(pixelValues, buffer.framebufferTextureWidth, buffer.framebufferTextureHeight);
		BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		int l = buffer.framebufferTextureHeight - buffer.framebufferHeight;
		bufferedimage.setRGB(0, 0, width, height, pixelValues, l*buffer.framebufferTextureWidth, buffer.framebufferTextureWidth);
		return bufferedimage;
	}

}

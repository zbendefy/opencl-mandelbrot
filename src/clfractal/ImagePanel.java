package clfractal;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	private int lastWidth = -1, lastHeight = -1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static byte[] getZeroList() {
		byte[] ret = new byte[256];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = 0;
		}
		return ret;
	}

	static byte[] getGradientList() {
		// order: black: 0,1,2...126,127,-128,-127,-126...-2,-1

		byte[] ret = new byte[256];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (byte) (i - 128);
		}
		return ret;
	}

	private static final byte[] zList = getZeroList();
	private static final byte[] bList = getGradientList();

	public BufferedImage image;

	public ImagePanel() {

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(image, 0, 0, null);
	}

	public void drawImage() {
		invalidate();
	}

	public void updateImageSize(int width, int height) {

		if (width != lastWidth || height != lastHeight)
		{
			IndexColorModel icm = new IndexColorModel(8, 256, zList, zList,
					bList);
			image = new BufferedImage(width, height,
					BufferedImage.TYPE_BYTE_INDEXED, icm);
			lastHeight = height;
			lastWidth = width;
			System.out.println("imagebuffer recreated");
		}
	}

	public byte[] getImageByteArray() {
		DataBuffer toArray = image.getRaster().getDataBuffer();
		return ((DataBufferByte) toArray).getData();
	}

}
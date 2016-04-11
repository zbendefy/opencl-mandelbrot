package clfractal;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

import javax.swing.JPanel;

public class ImagePanel extends JPanel implements MouseMotionListener, MouseWheelListener {

	private static final boolean isRGB = false;
	private IMouseEvents mouseEventListener = null;
	private int lastX = 0, lastY = 0;
	
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
		// order: black: 0,1,2...126,127,-128,-127,-126...-2,-1 :white

		byte[] ret = new byte[256];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (byte) (i - 128);
		}
		return ret;
	}

	private static final byte[] zList = getZeroList();
	private static final byte[] bList = getGradientList();
	private IndexColorModel icm;

	public BufferedImage image;

	public ImagePanel() {
		if (!isRGB) {
			icm = new IndexColorModel(8, 256, zList, zList, bList);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}

	public void drawImage() {
		invalidate();
	}

	public void updateImageSize() {
		
		if (this.getWidth() != lastWidth || this.getHeight() != lastHeight) {
			if (isRGB) {
				image = new BufferedImage(this.getWidth(), this.getHeight(),
						BufferedImage.TYPE_INT_RGB, null);
			} else {
				image = new BufferedImage(this.getWidth(), this.getHeight(),
						BufferedImage.TYPE_BYTE_INDEXED, icm);
			}
			lastHeight = this.getHeight();
			lastWidth = this.getWidth();
		}
	}

	public byte[] getImageByteArray() {
		if (image == null) updateImageSize();
		DataBuffer toArray = image.getRaster().getDataBuffer();
		return ((DataBufferByte) toArray).getData();
	}

	public void SetMouseListener(IMouseEvents listener)
	{
		mouseEventListener = listener;
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseEventListener != null)
		{
			mouseEventListener.OnDragView(e.getX() - lastX, e.getY() - lastY);
		}
		lastX = e.getX();
		lastY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		lastX = e.getX();
		lastY = e.getY();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent ev) {
		if (mouseEventListener != null)
		{
			mouseEventListener.OnWheelEvent(ev.getWheelRotation());
		}
	}

}
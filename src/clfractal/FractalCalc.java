package clfractal;

import static org.jocl.CL.*;

import java.awt.image.BufferedImage;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import clframework.common.CLContext;
import clframework.common.CLKernel;

public class FractalCalc {

	private int Iterations = 25;
	private double posx = 0, posy = 0, zoom = 2.0f;
	private ImagePanel pic = null;
	private boolean highPrecision = false;
	private boolean GLSharing = false;
	private long lastExecTime;
	private long lastTotalTime;

	private CLContext context = null;
	private CLKernel kernel = null;
	private static cl_mem memObjects[];

	private int platformid, deviceid;

	public FractalCalc(ImagePanel img) throws Exception {
		this.platformid = 0;
		this.deviceid = 0;
		this.pic = img;
		highPrecision = false;
		CLSources.setUse64Bit(highPrecision);
	}

	public FractalCalc(ImagePanel img, int platformid, int deviceid)
			throws Exception {
		this.platformid = platformid;
		this.deviceid = deviceid;
		this.pic = img;
		highPrecision = false;
		CLSources.setUse64Bit(highPrecision);
	}

	public void updateImage() throws Exception {
		if (context == null) {
			try {
				context = CLContext.createContext(platformid, deviceid);

			} catch (Exception e) {
				if (context != null) {
					context.delete();
				}
				throw e;
			}
		}

		if (kernel == null) {
			try {
				kernel = new CLKernel(context, "mandelbrot",
						new String[] { CLSources.getMandelbrotSource() },
						"-cl-fast-relaxed-math");
			} catch (Exception e) {
				if (kernel != null) {
					kernel.delete();
				}
				throw e;
			}
		}

		int width = pic.getSize().width;
		int height = pic.getSize().height;
		int n = width * height;

		pic.updateImageSize(width, height);

		long time1 = System.nanoTime();
		
		int srcArrayA[] = new int[3];
		float srcArrayB[] = new float[4];
		double srcArrayB_D[] = new double[4];
		byte dstArray[] = pic.getImageByteArray();

		srcArrayA[0] = Iterations; // 50 iterations
		srcArrayA[1] = pic.getSize().width; // resx
		srcArrayA[2] = pic.getSize().height; // resy
		srcArrayB[0] = (float) posx; // posx
		srcArrayB[1] = (float) posy; // posy
		srcArrayB[2] = (float) zoom; // zoom
		srcArrayB[3] = (float) height / (float) width; // aspect ratio
		srcArrayB_D[0] = posx; // posx
		srcArrayB_D[1] = posy; // posy
		srcArrayB_D[2] = zoom; // zoom
		srcArrayB_D[3] = (double) height / (double) width; // aspect ratio

		Pointer srcA = Pointer.to(srcArrayA);
		Pointer srcB;
		if (highPrecision)
			srcB = Pointer.to(srcArrayB_D);
		else
			srcB = Pointer.to(srcArrayB);
		Pointer dst = Pointer.to(dstArray);

		memObjects = new cl_mem[3];
		memObjects[0] = clCreateBuffer(context.getContext(), CL_MEM_READ_ONLY
				| CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 3, srcA, null);
		memObjects[1] = clCreateBuffer(context.getContext(), CL_MEM_READ_ONLY
				| CL_MEM_COPY_HOST_PTR, (highPrecision ? Sizeof.cl_double
				: Sizeof.cl_float) * 4, srcB, null);
		memObjects[2] = clCreateBuffer(context.getContext(), CL_MEM_WRITE_ONLY,
				Sizeof.cl_char * n, null, null);

		for (int i = 0; i < memObjects.length; i++) {
			clSetKernelArg(kernel.getKernel(), i, Sizeof.cl_mem,
					Pointer.to(memObjects[i]));
		}

		long global_work_size[] = new long[] { width, height };

		long time2 = System.nanoTime();
		
		clEnqueueNDRangeKernel(context.getCommandQueue(), kernel.getKernel(),
				2, null, global_work_size, null, 0, null, null);
		
		clEnqueueReadBuffer(context.getCommandQueue(), memObjects[2], CL_TRUE,
				0, width * height * Sizeof.cl_char, dst, 0, null, null);

		long time4 = System.nanoTime();
		
		for (int i = 0; i < memObjects.length; i++) {
			clReleaseMemObject(memObjects[i]);
		}

		lastTotalTime = time4 - time1;
		lastExecTime = time4 - time2;
		
		pic.repaint();
	}

	public void deleteResources() {
		try {
			context.delete();
			kernel.delete();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public int getIterations() {
		return Iterations;
	}

	public void setIterations(int iterations) {
		Iterations = iterations;
	}

	public double getPosx() {
		return posx;
	}

	public void setPosx(double posx) {
		this.posx = posx;
	}

	public void modPosx(double posx) {
		this.posx += posx * zoom;
	}

	public double getPosy() {
		return posy;
	}

	public void setPosy(double posy) {
		this.posy = posy;
	}

	public void modPosy(double posy) {
		this.posy += posy * zoom;
	}

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		if (zoom > 0 && zoom < 25)
			this.zoom = zoom;
	}

	public ImagePanel getPic() {
		return pic;
	}

	public void setPic(ImagePanel pic) {
		if (pic != null)
			this.pic = pic;
	}

	public boolean isHighPrecision() {
		return highPrecision;
	}

	public void setHighPrecision(boolean highPrecision) {
		if (this.highPrecision != highPrecision) {
			kernel.delete();
			kernel = null;
		}
		CLSources.setUse64Bit(highPrecision);
		this.highPrecision = highPrecision;
	}

	public boolean isGLSharing() {
		return GLSharing;
	}

	public void setGLSharing(boolean gLSharing) {
		GLSharing = gLSharing;
	}

	public long getLastExecTime() {
		return lastExecTime / 1000;
	}

	public long getLastTotalTime() {
		return lastTotalTime / 1000;
	}

	public int getPlatformid() {
		return platformid;
	}

	public int getDeviceid() {
		return deviceid;
	}
	
	

}

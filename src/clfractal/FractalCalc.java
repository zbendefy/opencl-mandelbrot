package clfractal;

import static org.jocl.CL.*;

import java.awt.image.BufferedImage;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import clframework.common.CLContext;
import clframework.common.CLKernel;

public class FractalCalc {

	public enum FractalModes {
		MANDELBROT, JULIA
	};

	FractalModes fractalMode;
	private int Iterations = 25;
	private int exponent = 2;
	private double posx = 0, posy = 0, zoom = 2.0f;
	private double savedposx = 0, savedposy = 0, savedzoom = 2.0f;
	private double juliaposx, juliaposy;
	private ImagePanel pic = null;
	private boolean highPrecision = false;
	private boolean GLSharing = false;
	private long lastExecTime;
	private long lastTotalTime;

	private CLContext context = null;
	private CLKernel kernel = null;
	private static cl_mem memObjects[] = new cl_mem[3];

	int srcArrayA[] = new int[4];
	float srcArrayB[] = new float[6];
	double srcArrayB_D[] = new double[6];
	long global_work_size[] = new long[] { 0, 0 };

	private int platformid, deviceid;

	public FractalCalc(ImagePanel img, int platformid, int deviceid)
			throws Exception {
		this.platformid = platformid;
		this.deviceid = deviceid;
		this.pic = img;
		highPrecision = false;
		CLSources.setUse64Bit(highPrecision);
		fractalMode = FractalModes.MANDELBROT;
	}

	public double[] getState() {
		return new double[] { posx, posy, zoom, savedposx, savedposy,
				savedzoom, juliaposx, juliaposy,
				fractalMode == FractalModes.JULIA ? 1.0 : -1.0, exponent };
	}

	public void restoreState(double[] state) {
		posx = state[0];
		posy = state[1];
		zoom = state[2];
		savedposx = state[3];
		savedposy = state[4];
		savedzoom = state[5];
		juliaposx = state[6];
		juliaposy = state[7];
		fractalMode = state[8] > 0 ? FractalModes.JULIA
				: FractalModes.MANDELBROT;
		exponent = (int) state[9];
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
				String kernelname = fractalMode == FractalModes.JULIA ? "julia"
						: "mandelbrot";
				if (exponent != 2) {
					kernelname += "N";
				}
				kernel = new CLKernel(context, kernelname,
						new String[] { CLSources.getSource() },
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

		srcArrayA[0] = Iterations; // 50 iterations
		srcArrayA[1] = pic.getSize().width; // resx
		srcArrayA[2] = pic.getSize().height; // resy
		srcArrayA[3] = exponent; // resy
		srcArrayB[0] = (float) posx; // posx
		srcArrayB[1] = (float) posy; // posy
		srcArrayB[2] = (float) zoom; // zoom
		srcArrayB[3] = (float) height / (float) width; // aspect ratio
		srcArrayB[4] = (float) juliaposx;
		srcArrayB[5] = (float) juliaposy;
		srcArrayB_D[0] = posx; // posx
		srcArrayB_D[1] = posy; // posy
		srcArrayB_D[2] = zoom; // zoom
		srcArrayB_D[3] = (double) height / (double) width; // aspect ratio
		srcArrayB_D[4] = juliaposx;
		srcArrayB_D[5] = juliaposy;

		Pointer srcA = Pointer.to(srcArrayA);
		Pointer srcB = highPrecision ? Pointer.to(srcArrayB_D) : Pointer
				.to(srcArrayB);
		Pointer dst = Pointer.to(pic.getImageByteArray());

		memObjects[0] = clCreateBuffer(context.getContext(), CL_MEM_READ_ONLY
				| CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * srcArrayA.length, srcA,
				null);
		memObjects[1] = clCreateBuffer(context.getContext(), CL_MEM_READ_ONLY
				| CL_MEM_COPY_HOST_PTR, (highPrecision ? Sizeof.cl_double
				: Sizeof.cl_float) * srcArrayB.length, srcB, null);
		memObjects[2] = clCreateBuffer(context.getContext(), CL_MEM_WRITE_ONLY,
				Sizeof.cl_char * n, null, null);

		for (int i = 0; i < memObjects.length; i++) {
			clSetKernelArg(kernel.getKernel(), i, Sizeof.cl_mem,
					Pointer.to(memObjects[i]));
		}

		global_work_size[0] = width;
		global_work_size[1] = height;

		long time2 = System.nanoTime();

		clEnqueueNDRangeKernel(context.getCommandQueue(), kernel.getKernel(),
				2, null, global_work_size, null, 0, null, null);

		clEnqueueReadBuffer(context.getCommandQueue(), memObjects[2], CL_TRUE,
				0, width * height * Sizeof.cl_char, dst, 0, null, null);

		long time3 = System.nanoTime();

		for (int i = 0; i < memObjects.length; i++) {
			clReleaseMemObject(memObjects[i]);
			memObjects[i] = null;
		}

		lastTotalTime = time3 - time1;
		lastExecTime = time3 - time2;

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
		return lastExecTime / 1000000;
	}

	public long getLastTotalTime() {
		return lastTotalTime / 1000000;
	}

	public int getPlatformid() {
		return platformid;
	}

	public int getDeviceid() {
		return deviceid;
	}

	public void switchMode(FractalModes mode) {
		if (fractalMode == mode) {
			return;
		}

		if (mode == FractalModes.JULIA) {
			savedposx = posx;
			savedposy = posy;
			savedzoom = zoom;
			juliaposx = posx;
			juliaposy = posy;
			posx = 0;
			posy = 0;
			zoom = 2.0f;
		} else if (mode == FractalModes.MANDELBROT) {
			posx = savedposx;
			posy = savedposy;
			zoom = savedzoom;
		}

		kernel = null;

		fractalMode = mode;
	}

	public void setExponent(int exp) {
		if ((exponent == 2 && exp != 2) || (exp == 2 && exponent != 2)) {
			kernel = null;
		}
		exponent = exp;

	}
}

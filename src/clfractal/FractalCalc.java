package clfractal;

import static org.jocl.CL.*;

import java.util.ArrayList;
import java.util.List;

import org.jocl.Pointer;
import org.jocl.Sizeof;

import clframework.common.CLContext;
import clframework.common.CLDevice;
import clframework.common.CLKernel;
import clframework.common.MemObject;

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
	private boolean highPrecision = false;
	private long lastExecTime;
	private long lastTotalTime;
	private int width = 1, height = 1;

	private CLDevice clDevice = null;
	private CLContext context = null;
	private CLKernel kernel = null;
	private MemObject intParams, floatParams, imgBuffer;

	int srcArrayA[] = new int[4];
	float srcArrayB[] = new float[6];
	double srcArrayB_D[] = new double[6];
	long global_work_size[] = new long[] { 0, 0 };
	long local_work_size[] = new long[] { 8, 8 };
	boolean useExplicitLocalWorkSize = true;
	String kernelname = "mandelbrot";

	private void UpdateKernelName(){
	    kernelname = fractalMode == FractalModes.JULIA ? "julia" : "mandelbrot";
	    if (exponent != 2) {
	        kernelname += "N";
	    }
	}
	
	public CLDevice getDevice(){return clDevice;}

	public FractalCalc(ImagePanel img, CLDevice device) throws Exception {
		this.clDevice = device;

		context = new CLContext(clDevice);

		try {
			int workDimensions = clDevice.getDeviceIntProperty(CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
			long[] sizes = clDevice.getDeviceLongArrayProperty(CL_DEVICE_MAX_WORK_ITEM_SIZES, workDimensions);
			long workgroupsize = clDevice.getDeviceLongProperty(CL_DEVICE_MAX_WORK_GROUP_SIZE);

			useExplicitLocalWorkSize = (workDimensions >= 2 && sizes.length >= 2 && sizes[0] >= 8 && sizes[1] >= 8
					&& workgroupsize >= 64);
		} catch (Exception e) {
			useExplicitLocalWorkSize = false;
		}
		
		highPrecision = false;
		fractalMode = FractalModes.MANDELBROT;
	}

	public double[] getState() {
		return new double[] { posx, posy, zoom, savedposx, savedposy, savedzoom, juliaposx, juliaposy,
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
		fractalMode = state[8] > 0 ? FractalModes.JULIA : FractalModes.MANDELBROT;
		exponent = (int) state[9];
	}

	public void onResize(int w, int h) {
		if (width == w || height == h || w < 1 || h < 1)
			return;

		width = w;
		height = h;
		
		if ( kernel != null && imgBuffer != null){
			imgBuffer.delete();
			imgBuffer = null;
		}
	}

	public void drawImage(byte[] result) throws Exception {
		if (context == null || result == null || result.length < 1) {
			return;
		}
		
		if ( kernel == null){
			String compileOptions = "-cl-fast-relaxed-math";
			if ( highPrecision)
				compileOptions += " -DUSE_HIGH_PRECISION";
			
			kernel = new CLKernel(context, new String[] { CLSources.readLocalFile("/clsrc/mandelbrot.cl") },
					new String[] { "mandelbrot", "mandelbrotN", "julia", "juliaN" }, compileOptions);
		}

		final int numPixels = width * height;
		final long time1 = System.nanoTime();

		srcArrayA[0] = Iterations; // iteration count
		srcArrayA[1] = width; // resx
		srcArrayA[2] = height; // resy
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
		Pointer srcB = highPrecision ? Pointer.to(srcArrayB_D) : Pointer.to(srcArrayB);
		Pointer dst = Pointer.to(result);

		intParams = MemObject.createMemObjectReadOnly(context, Sizeof.cl_int * srcArrayA.length, srcA);
		floatParams = MemObject.createMemObjectReadOnly(context, (highPrecision ? Sizeof.cl_double : Sizeof.cl_float) * srcArrayB.length, srcB);

		if (imgBuffer == null) {
			imgBuffer = MemObject.createMemObjectWriteOnly(context, Sizeof.cl_char * numPixels);
		}

		List<MemObject> parameters = new ArrayList<MemObject>(3);
		parameters.add(intParams);
		parameters.add(floatParams);
		parameters.add(imgBuffer);

		if (useExplicitLocalWorkSize) {
			global_work_size[0] = width
					+ ((width % local_work_size[0]) != 0 ? (local_work_size[0] - (width % local_work_size[0])) : 0);
			global_work_size[1] = height
					+ ((height % local_work_size[1]) != 0 ? (local_work_size[1] - (height % local_work_size[1])) : 0);
		} else {
			global_work_size[0] = width;
			global_work_size[1] = height;
		}

		final long time2 = System.nanoTime();
		kernel.enqueueNDRangeKernel(kernelname, parameters, global_work_size, null, useExplicitLocalWorkSize ? local_work_size : null);

		imgBuffer.ReadBufferWithBlocking(dst);

		final long time3 = System.nanoTime();

		intParams.delete();
		intParams = null;
		floatParams.delete();
		floatParams = null;

		lastTotalTime = time3 - time1;
		lastExecTime = time3 - time2;
	}

	public void deleteResources() {
		try {
			context.delete();
			kernel.delete();
			if ( imgBuffer!= null )
				imgBuffer.delete();
			if ( intParams!= null )
				intParams.delete();
			if ( floatParams!= null )
				floatParams.delete();
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

	public boolean isHighPrecision() {
		return highPrecision;
	}

	public void setHighPrecision(boolean highPrecision) {
		if (this.highPrecision != highPrecision) {
			kernel.delete();
			kernel = null;
			if ( imgBuffer != null ){
				imgBuffer.delete();
				imgBuffer = null;
			}
		}
		this.highPrecision = highPrecision;
	}

	public long getLastExecTime() {
		return lastExecTime / 1000000;
	}

	public long getLastTotalTime() {
		return lastTotalTime / 1000000;
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

		fractalMode = mode;
		
		UpdateKernelName();
	}

	public void setExponent(int exp) {
		exponent = exp;
		UpdateKernelName();
	}
}

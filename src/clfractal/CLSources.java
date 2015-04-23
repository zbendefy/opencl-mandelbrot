package clfractal;

import java.io.InputStream;

public class CLSources {

	public static String separator = System.getProperty("line.separator");
	private static boolean use64Bit = false;
	
	private static String readLocalFile(String fname) {
		InputStream input = CLSources.class.getResourceAsStream(fname);

		java.util.Scanner s = new java.util.Scanner(input);
		s.useDelimiter("\\A");
		String ret = s.hasNext() ? s.next() : "";
		s.close();
		return ret;
	}
	
	public static String getMandelbrotSource()
	{
		String file = readLocalFile("/clsrc/mandelbrot.cl");
		
		file = file.replace("%64bit_pragma%", use64Bit ? "#pragma OPENCL EXTENSION cl_khr_fp64 : enable" : "");
		file = file.replace("%numberformat%", use64Bit ? "double" : "float");
		
		return file;
	}

	public static boolean isUse64Bit() {
		return use64Bit;
	}

	public static void setUse64Bit(boolean use64Bit) {
		CLSources.use64Bit = use64Bit;
	}
	
	
}

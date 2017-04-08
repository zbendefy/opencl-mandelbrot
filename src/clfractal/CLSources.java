package clfractal;

import java.io.InputStream;

public class CLSources {

	public static String separator = System.getProperty("line.separator");
	
	public static String readLocalFile(String fname) {
		InputStream input = CLSources.class.getResourceAsStream(fname);

		java.util.Scanner s = new java.util.Scanner(input);
		s.useDelimiter("\\A");
		String ret = s.hasNext() ? s.next() : "";
		s.close();
		return ret;
	}
}

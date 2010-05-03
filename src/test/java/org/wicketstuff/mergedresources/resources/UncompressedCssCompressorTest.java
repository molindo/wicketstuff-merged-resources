package org.wicketstuff.mergedresources.resources;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Test;

public class UncompressedCssCompressorTest {

	@Test
	public void testCompress() throws Exception {
		String css = "a { color: #FFFFFF; }\n";
		
		Charset latin1 = Charset.forName("ISO-8859-1");

		byte[] original = css.getBytes();
		byte[] processed = new UncompressedCssCompressor().compress(css.getBytes(latin1.name()), latin1);
		
		assertTrue(Arrays.toString(original) + " - " + Arrays.toString(processed), 
				Arrays.equals(original, processed));
	}

}

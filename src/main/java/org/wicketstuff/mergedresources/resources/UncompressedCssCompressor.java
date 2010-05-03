package org.wicketstuff.mergedresources.resources;

import java.nio.charset.Charset;

public class UncompressedCssCompressor implements ICssCompressor {

	public byte[] compress(byte[] original, Charset charset) {
		if (!ICssCompressor.EXPECTED_CHARSET.equals(charset)) {
			return new String(original, charset).getBytes(ICssCompressor.EXPECTED_CHARSET);
		} else {
			return original;
		}
	}
}

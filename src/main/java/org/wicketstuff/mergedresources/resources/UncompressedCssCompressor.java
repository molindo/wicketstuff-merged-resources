package org.wicketstuff.mergedresources.resources;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.wicket.WicketRuntimeException;

public class UncompressedCssCompressor implements ICssCompressor {

	public byte[] compress(byte[] original, Charset charset) {
		if (!ICssCompressor.EXPECTED_CHARSET.equals(charset)) {
			try {
				return new String(original, charset.name()).getBytes(ICssCompressor.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new WicketRuntimeException("unexpected encoding from Charset.name()?", e);
			}
		} else {
			return original;
		}
	}
}

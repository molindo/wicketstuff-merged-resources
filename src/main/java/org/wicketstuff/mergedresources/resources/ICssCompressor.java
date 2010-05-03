package org.wicketstuff.mergedresources.resources;

import java.nio.charset.Charset;

import org.wicketstuff.mergedresources.ResourceMount;

/**
 * interface to add your preferred CSS compressor. Use 
 * {@link ResourceMount#setCssCompressor(org.apache.wicket.Application, ICssCompressor)}
 * to use a CSS compressor
 */
public interface ICssCompressor {
	public static final Charset UTF_8 = Charset.forName("utf-8");
	public static final Charset EXPECTED_CHARSET = UTF_8;
	
	/**
	 * 
	 * @param original byte array of original content
	 * @param charset charset of bytes in original content
	 * @return bytes of compressed content in {@link #EXPECTED_CHARSET}
	 */
	public byte[] compress(byte[] original, Charset charset);
}

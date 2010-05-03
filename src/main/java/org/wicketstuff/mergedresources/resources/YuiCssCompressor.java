package org.wicketstuff.mergedresources.resources;

import java.nio.charset.Charset;

import org.wicketstuff.mergedresources.util.YuiCompressorUtil;

public class YuiCssCompressor implements ICssCompressor {

	public byte[] compress(byte[] original, Charset charset) {
		return YuiCompressorUtil.compress(original, charset);
	}

}

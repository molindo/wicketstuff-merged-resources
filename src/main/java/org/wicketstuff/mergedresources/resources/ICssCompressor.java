/**
 * Copyright 2016 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	 * @param original
	 *            byte array of original content
	 * @param charset
	 *            charset of bytes in original content
	 * @return bytes of compressed content in {@link #EXPECTED_CHARSET}
	 */
	public byte[] compress(byte[] original, Charset charset);
}

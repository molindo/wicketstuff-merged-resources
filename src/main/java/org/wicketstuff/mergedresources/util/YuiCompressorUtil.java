/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.mergedresources.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import com.yahoo.platform.yui.compressor.CssCompressor;

public class YuiCompressorUtil {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(YuiCompressorUtil.class);
	
	private YuiCompressorUtil() {
		// no instances
	}
	
	public static String compress(final String toCompress) {
		final StringWriter writer = new StringWriter((int) (toCompress.length() * 0.8));
		try {
			new CssCompressor(new StringReader(toCompress)).compress(writer, 0);
		} catch (final Exception e) {
			log.warn("Could not compress merged CSS stream, using uncompressed content", e);
			return toCompress;
		}
		return writer.toString();
	}
	
	public static byte[] compress(final byte[] toCompress, Charset charset) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(toCompress.length);
		
		final OutputStreamWriter writer = new OutputStreamWriter(out);
		try {
			new CssCompressor(new InputStreamReader(new ByteArrayInputStream(toCompress), charset)).compress(writer, 0);
			writer.flush();
			writer.close();
		} catch (final Exception e) {
			log.warn("Could not compress merged CSS stream, using uncompressed content", e);
			return toCompress;
		}
		return out.toByteArray();
	}
}

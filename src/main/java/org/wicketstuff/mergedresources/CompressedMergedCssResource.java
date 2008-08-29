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
package org.wicketstuff.mergedresources;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.util.resource.IResourceStream;

import com.yahoo.platform.yui.compressor.CssCompressor;

public class CompressedMergedCssResource extends CompressedMergedResource {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(CompressedMergedCssResource.class);

	public CompressedMergedCssResource(Class<?> scope, final String path, final Locale locale, final String style, final Class<?>[] scopes, final String[] files, int cacheDuration) {
		super(scope, path, locale, style, scopes, files, cacheDuration);
	}

	@Override
	protected IResourceStream newResourceStream(final Locale locale, final String style, final Class<?>[] scopes, final String[] files) {
		return new MergedResourceStream(scopes, files, locale, style) {
			private static final long serialVersionUID = 1L;

			@Override
			protected String toContent(final String content) {
				// use the JS settings for CSS
				if (Application.get().getResourceSettings()
						.getStripJavascriptCommentsAndWhitespace()) {
					final StringWriter writer = new StringWriter((int) (content.length() * 0.8));
					try {
						new CssCompressor(new StringReader(content)).compress(writer, 0);
					} catch (final IOException e) {
						log.warn("Could not compress merged CSS stream, using uncompressed content", e);
						return content;
					}
					return writer.toString();
				} else {
					return content;
				}
			}
		};
	}
}

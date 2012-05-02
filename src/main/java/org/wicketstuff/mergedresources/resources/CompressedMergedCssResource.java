/**
 * Copyright 2010 Molindo GmbH
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

import org.apache.wicket.Application;
import org.apache.wicket.util.resource.IResourceStream;
import org.wicketstuff.mergedresources.ResourceMount;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

import java.util.Locale;

public class CompressedMergedCssResource extends CompressedMergedResource {

	private static final long serialVersionUID = 1L;

	public CompressedMergedCssResource(Class<?> scope, final String path, final Locale locale, final String style,
									   ResourceSpec[] specs, int cacheDuration, IResourcePreProcessor preProcessor) {
		super(scope, path, locale, style, specs, cacheDuration, preProcessor);
	}

	@Override
	protected IResourceStream newResourceStream(final Locale locale, final String style, final ResourceSpec[] specs,
												IResourcePreProcessor preProcessor) {
		return new MergedResourceStream(specs, locale, style, preProcessor) {
			private static final long serialVersionUID = 1L;

			@Override
			protected byte[] toContent(final byte[] content) {
				ICssCompressor compressor = ResourceMount.getCssCompressor(Application.get());
				if (compressor != null) {
					return compressor.compress(content, ICssCompressor.UTF_8);
				} else {
					return content;
				}
			}

			@Override
			public String getContentType() {
				return "text/css";
			}
		};
	}
}

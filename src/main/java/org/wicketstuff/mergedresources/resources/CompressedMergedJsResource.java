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
package org.wicketstuff.mergedresources.resources;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.util.resource.IResourceStream;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

public class CompressedMergedJsResource extends CompressedMergedResource {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(CompressedMergedJsResource.class);

	/**
	 * @deprecated use ResourceSpec[] instead of scopes[] and files[]
	 */
	@Deprecated
	public CompressedMergedJsResource(Class<?> scope, final String path,
			final Locale locale, final String style, final Class<?>[] scopes,
			final String[] files, int cacheDuration) {
		this(scope, path, locale, style, ResourceSpec.toResourceSpecs(scopes, files), cacheDuration, null);
	}

	public CompressedMergedJsResource(Class<?> scope, final String path,
			final Locale locale, String style, final ResourceSpec[] specs, int cacheDuration, IResourcePreProcessor preProcessor) {
		super(scope, path, locale, style, specs, cacheDuration, preProcessor);
	}
	
	@Override
	protected IResourceStream newResourceStream(final Locale locale,
			final String style, final ResourceSpec[] specs, IResourcePreProcessor preProcessor) {
		return new MergedResourceStream(specs, locale, style, preProcessor) {
			private static final long serialVersionUID = 1L;

			@Override
			protected byte[] toContent(final byte[] content) {
				try {
					return Application.get().getResourceSettings().getJavascriptCompressor().compress(new String(content)).getBytes();
				} catch (Exception e) {
					log.error("Error while stripping content", e);
					return content;
				}
			}
			
			@Override
			public String getContentType() {
				return "application/x-javascript";
			}
		};
	}
}

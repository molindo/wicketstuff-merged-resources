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
import org.apache.wicket.markup.html.CompressedPackageResource;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.resource.IResourceStream;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

public class CompressedMergedResource extends CompressedPackageResource {

	private static final long serialVersionUID = 1L;

	private final IResourceStream _mergedResourceStream;

	private int _cacheDuration;

	/**
	 * @deprecated use ResourceSpec[] instead of scopes[] and files[]
	 */
	@Deprecated
	public CompressedMergedResource(Class<?> scope, final String path, final Locale locale, final String style, final Class<?>[] scopes, final String[] files, int cacheDuration) {
		this(scope, path, locale, style, ResourceSpec.toResourceSpecs(scopes, files), cacheDuration, null);
	}

	public CompressedMergedResource(Class<?> scope, final String path, final Locale locale, final String style, final ResourceSpec[] specs, int cacheDuration, IResourcePreProcessor preProcessor) {
		super(scope, path, locale, style);
		_cacheDuration = cacheDuration;
		_mergedResourceStream = newResourceStream(locale, style, specs, preProcessor);
	}
	
	protected IResourceStream newResourceStream(final Locale locale, final String style, final ResourceSpec[] specs, IResourcePreProcessor preProcessor) {
		return new MergedResourceStream(specs, locale, style, preProcessor);
	}
	
	@Override
	protected IResourceStream getPackageResourceStream() {
		return _mergedResourceStream;
	}

	@Override
	public int getCacheDuration() {
		return _cacheDuration;
	}
	
	protected void setHeaders(WebResponse response)
	{
		super.setHeaders(response);
		if (!Application.get().getResourceSettings().getDisableGZipCompression())
		{
			response.setHeader("Vary", "Accept-Encoding");
		}
		response.setHeader("Cache-control", "public");
	}
}

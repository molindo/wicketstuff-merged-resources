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

import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Duration;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

import java.util.Locale;

public class CompressedMergedResource extends PackageResource {

	private static final long serialVersionUID = 1L;

	private final IResourceStream _mergedResourceStream;
	private int _cacheDuration;

	public CompressedMergedResource(Class<?> scope, final String path, final Locale locale, final String style,
			final ResourceSpec[] specs, int cacheDuration, IResourcePreProcessor preProcessor) {
		super(scope, path, locale, style, null);
		_cacheDuration = cacheDuration;
		_mergedResourceStream = newResourceStream(locale, style, specs, preProcessor);
	}

	@Override
	public IResourceStream getResourceStream() {
		return _mergedResourceStream;
	}

	protected IResourceStream newResourceStream(final Locale locale, final String style, final ResourceSpec[] specs,
			IResourcePreProcessor preProcessor) {
		return new MergedResourceStream(specs, locale, style, preProcessor);
	}

	@Override
	protected void configureCache(ResourceResponse data, Attributes attributes) {
		data.setCacheDuration(Duration.seconds(_cacheDuration));
		super.configureCache(data, attributes);
	}
}

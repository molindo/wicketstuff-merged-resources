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

import java.util.Locale;

import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "super type is sufficient")
public class CachedCompressedResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	private final int _cacheDuration;
	private final IResourcePreProcessor _preProcessor;

	public CachedCompressedResourceReference(Class<?> scope, String path, Locale locale, String style,
			int cacheDuration, IResourcePreProcessor preProcessor) {
		super(scope, path, locale, style);
		_cacheDuration = cacheDuration;
		_preProcessor = preProcessor;
	}

	@Override
	protected Resource newResource() {
		return new CachedCompressedResource(getScope(), getName(), getLocale(), getStyle(), _cacheDuration,
				_preProcessor);
	}

	public int getCacheDuration() {
		return _cacheDuration;
	}

}
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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

import java.util.Locale;

@SuppressWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "super type is sufficient")
public class MergedResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	private final ResourceSpec[] _specs;
	private final int _cacheDuration;
	private final IResourcePreProcessor _preProcessor;

	public MergedResourceReference(String name, Locale locale, String style, ResourceSpec[] specs, int cacheDuration,
			IResourcePreProcessor preProcessor) {
		this(MergedResourceReference.class, name, locale, style, specs, cacheDuration, preProcessor);
	}

	public MergedResourceReference(Class<?> scope, String name, Locale locale, String style, ResourceSpec[] specs,
			int cacheDuration, IResourcePreProcessor preProcessor) {
		super(scope, name, locale, style, null);
		_specs = specs;
		_cacheDuration = cacheDuration;
		_preProcessor = preProcessor;
	}

	@Override
	public IResource getResource() {
		return new MergedResource(getScope(), getName(), getLocale(), getStyle(), _specs, _cacheDuration, _preProcessor);
	}

	public ResourceSpec[] getMergedSpecs() {
		return _specs;
	}

	public int getCacheDuration() {
		return _cacheDuration;
	}

	public IResourcePreProcessor getPreProcessor() {
		return _preProcessor;
	}

}
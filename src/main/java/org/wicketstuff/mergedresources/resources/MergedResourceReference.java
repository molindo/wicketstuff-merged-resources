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
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "super type is sufficient")
public class MergedResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	private final ResourceSpec[] _specs;
	private final int _cacheDuration;
	private final IResourcePreProcessor _preProcessor;

	@Deprecated
	public MergedResourceReference(Class<?> scope, String path, Locale locale, String style, Class<?>[] scopes,
			String[] files, int cacheDuration) {
		this(scope, path, locale, style, ResourceSpec.toResourceSpecs(scopes, files), cacheDuration, null);
	}

	public MergedResourceReference(String name, Locale locale, String style, ResourceSpec[] specs, int cacheDuration,
			IResourcePreProcessor preProcessor) {
		this(MergedResourceReference.class, name, locale, style, specs, cacheDuration, preProcessor);
	}

	public MergedResourceReference(Class<?> scope, String name, Locale locale, String style, ResourceSpec[] specs,
			int cacheDuration, IResourcePreProcessor preProcessor) {
		super(scope, name, locale, style);
		_specs = specs;
		_cacheDuration = cacheDuration;
		_preProcessor = preProcessor;
	}

	@Override
	protected Resource newResource() {
		return new MergedResource(getScope(), getName(), getLocale(), getStyle(), _specs, _cacheDuration, _preProcessor);
	}

	@Deprecated
	public Class<?>[] getMergedScopes() {
		return ResourceSpec.toScopes(_specs);
	}

	@Deprecated
	public String[] getMergedFiles() {
		return ResourceSpec.toFiles(_specs);
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
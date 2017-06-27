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

import java.util.Locale;

import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "super type is sufficient")
public class MergedResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	private final ResourceSpec[] _specs;
	private final int _cacheDuration;
	private final IResourcePreProcessor _preProcessor;

	@Deprecated
	public MergedResourceReference(final Class<?> scope, final String path, final Locale locale, final String style, final Class<?>[] scopes, final String[] files, final int cacheDuration) {
		this(scope, path, locale, style, ResourceSpec.toResourceSpecs(scopes, files), cacheDuration, null);
	}

	public MergedResourceReference(final String name, final Locale locale, final String style, final ResourceSpec[] specs, final int cacheDuration, final IResourcePreProcessor preProcessor) {
		this(MergedResourceReference.class, name, locale, style, specs, cacheDuration, preProcessor);
	}

	public MergedResourceReference(final Class<?> scope, final String name, final Locale locale, final String style, final ResourceSpec[] specs, final int cacheDuration, final IResourcePreProcessor preProcessor) {
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

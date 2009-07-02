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

import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.wicketstuff.mergedresources.ResourceSpec;

public class MergedResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	private final ResourceSpec[] _specs;
	private int _cacheDuration;

	@Deprecated
	public MergedResourceReference(Class<?> scope, String path, Locale locale, String style, Class<?>[] scopes, String[] files, int cacheDuration) {
		this(scope, path, locale, style, ResourceSpec.toResourceSpecs(scopes, files), cacheDuration);
	}

	public MergedResourceReference(String name, Locale locale, String style, ResourceSpec[] specs, int cacheDuration) {
		this(MergedResourceReference.class, name, locale, style, specs, cacheDuration);
	}
	
	public MergedResourceReference(Class<?> scope, String name, Locale locale, String style, ResourceSpec[] specs, int cacheDuration) {
		super(scope, name, locale, style);
		_specs = specs;
		_cacheDuration = cacheDuration;
	}
	
	@Override
	protected Resource newResource() {
		return new MergedResource(getScope(), getName(), getLocale(), getStyle(), _specs, _cacheDuration);
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
	
	
}
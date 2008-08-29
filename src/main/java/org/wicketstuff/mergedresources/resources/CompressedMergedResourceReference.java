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

public class CompressedMergedResourceReference extends MergedResourceReference {

	private static final long serialVersionUID = 1L;

	public CompressedMergedResourceReference(String path, Locale locale, String style, Class<?>[] scopes, String[] files, int cacheDuration) {
		super(CompressedMergedResourceReference.class, path, locale, style, scopes, files, cacheDuration);
	}

	@Override
	protected Resource newResource() {
		return new CompressedMergedResource(getScope(), getName(), getLocale(), getStyle(), getMergedScopes(), getMergedFiles(), getCacheDuration());
	}
}
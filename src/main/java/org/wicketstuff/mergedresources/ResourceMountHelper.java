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
package org.wicketstuff.mergedresources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.resource.ISharedResourceRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.mergedresources.resources.CompressedMergedCssResourceReference;
import org.wicketstuff.mergedresources.resources.CompressedMergedJsResourceReference;
import org.wicketstuff.mergedresources.resources.CompressedMergedResourceReference;
import org.wicketstuff.mergedresources.resources.MergedResourceReference;
import org.wicketstuff.mergedresources.util.RedirectStrategy;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider.VersionException;


public class ResourceMountHelper {
	
	/**
	 * used if resource versioning is active
	 */
	// don't use more than that, see https://issues.apache.org/jira/browse/WICKET-1777
	private static int AGGRESSIVE_CACHE_DURATION = Integer.MAX_VALUE / 1000;
	
	/**
	 * used if versioning is disabled or no version was found
	 */
	private static int NORMAL_CACHE_DURATION = 3600;
	
	private static final HashSet<String> COMPRESS_SUFFIXES = new HashSet<String>(Arrays
			.asList("html", "css", "js", "xml"));
	
	private WebApplication _application;
	private IResourceVersionProvider _versionProvider;

	public ResourceMountHelper (WebApplication application, IResourceVersionProvider versionProvider) {
		_application = application;
		_versionProvider = versionProvider;
	}
	
	public ResourceReference mountMergedSharedResource(final String mountPrefix, final String path, final boolean detectVersion, final Class<?>[] scopes, final String[] files) {
		return mountMergedSharedResource(mountPrefix, path, detectVersion, scopes, files, true);
	}

	public ResourceReference mountMergedSharedResource(String mountPrefix, final String path, final boolean detectVersion, final Class<?>[] scopes, final String[] files, final boolean versionRequired) {

		if (scopes.length == 0 || files.length != scopes.length) {
			throw new IllegalArgumentException("arrays must not be empty and of equal size");
		}

		mountPrefix = (mountPrefix.startsWith("/") ? "" : "/") + mountPrefix + (mountPrefix.endsWith("/") ? "" : "/");
		
		final String unversionedPath = mountPrefix + path;
		String versionedPath = unversionedPath;
		int version = 0;
		if (detectVersion) {
			for (int i = 0; i < scopes.length; i++) {
				try {
					final int v = _versionProvider.getVersion(scopes[i], files[i]);
					if (v > version) {
						version = v;
					}
				} catch (final VersionException e) {
					if (versionRequired) {
						throw new WicketRuntimeException(e);
					}
				}
			}
			if (version > 0) {
				versionedPath = getVersionedPath(mountPrefix + path, version);
			}
		}

		final ResourceReference ref = getResourceReference(path, scopes, files, version > 0);
		
		_application.getSharedResources()
				.add(ref.getScope(), ref.getName(), ref.getLocale(), ref.getStyle(), ref.getResource());

		// 
		final ArrayList<String> mergedKeys = new ArrayList<String>(files.length);
		for (int i = 0; i < files.length; i++) {
			mergedKeys.add(new ResourceReference(scopes[i], files[i]) {

				private static final long serialVersionUID = 1L;

				@Override
				protected Resource newResource() {
					return ref.getResource();
				}

			}.getSharedResourceKey());
		}

		_application.mount(new SharedResourceRequestTargetUrlCodingStrategy(versionedPath, ref.getSharedResourceKey()) {

			@Override
			public boolean matches(final IRequestTarget requestTarget) {
				if (requestTarget instanceof ISharedResourceRequestTarget) {
					final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget) requestTarget;
					return super.matches(requestTarget)
							|| mergedKeys.contains(target.getResourceKey());
				} else {
					return false;
				}
			}

		});

		// redirect to orig page
		if (!unversionedPath.equals(versionedPath)) {
			_application.mount(new RedirectStrategy(unversionedPath, versionedPath));
		}

		return ref;
	}

	private ResourceReference getResourceReference(final String path,
			final Class<?>[] scopes, final String[] files, boolean aggressiveCaching) {
		final ResourceReference ref;
		int cacheDuration = getCacheDuration(aggressiveCaching);
		if (doCompress(path)) {
			if (isCSS(path)) {
				ref = new CompressedMergedCssResourceReference(path, null, null, 
						scopes, files, cacheDuration);
			} else if (isJS(path)) {
				ref = new CompressedMergedJsResourceReference(path, null, null, 
						scopes, files, cacheDuration);
			} else {
				ref = new CompressedMergedResourceReference(path, null, null, 
						scopes, files, cacheDuration);
			}
		} else {
			ref = new MergedResourceReference(ResourceMountHelper.class, path, null, null, scopes, files, cacheDuration);
		}
		ref.bind(_application);
		return ref;
	}

	protected int getCacheDuration(boolean aggressiveCaching) {
		return aggressiveCaching ? AGGRESSIVE_CACHE_DURATION : NORMAL_CACHE_DURATION;
	}


	protected String getVersionedPath(final Class<?> scope, final String fileName) {
		try {
			return getVersionedPath(fileName, _versionProvider.getVersion(scope, fileName));
		} catch (final VersionException e) {
			throw new WicketRuntimeException(e);
		}
	}

	protected String getVersionedPath(final Class<?> scope, final String fileName, final String filePath) {
		try {
			return getVersionedPath(filePath, _versionProvider.getVersion(scope, fileName));
		} catch (final VersionException e) {
			throw new WicketRuntimeException(e);
		}
	}

	protected String getVersionedPath(final String filePath, final int version) {
		return Strings.beforeLast(filePath, '.') + "-" + version + "."
				+ Strings.afterLast(filePath, '.');	}

	protected boolean doCompress(final String file) {
		return COMPRESS_SUFFIXES.contains(Strings.afterLast(file, '.'));
	}
	
	protected boolean isJS(final String file) {
		return file.endsWith(".js");
	}
	
	protected boolean isCSS(final String file) {
		return file.endsWith(".css");
	}
}

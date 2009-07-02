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
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.resource.SharedResourceRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.wicketstuff.mergedresources.resources.CachedCompressedCssResourceReference;
import org.wicketstuff.mergedresources.resources.CachedCompressedJsResourceReference;
import org.wicketstuff.mergedresources.resources.CachedCompressedResourceReference;
import org.wicketstuff.mergedresources.resources.CachedResourceReference;
import org.wicketstuff.mergedresources.resources.CompressedMergedCssResourceReference;
import org.wicketstuff.mergedresources.resources.CompressedMergedJsResourceReference;
import org.wicketstuff.mergedresources.resources.CompressedMergedResourceReference;
import org.wicketstuff.mergedresources.resources.MergedResourceReference;
import org.wicketstuff.mergedresources.util.MergedResourceRequestTargetUrlCodingStrategy;
import org.wicketstuff.mergedresources.util.RedirectStrategy;
import org.wicketstuff.mergedresources.versioning.AbstractResourceVersion;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.WicketVersionProvider;
import org.wicketstuff.mergedresources.versioning.AbstractResourceVersion.IncompatibleVersionsException;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider.VersionException;


public class ResourceMountHelper {
	
	/**
	 * used if resource versioning is active
	 */
	// use a year beginning from wicket 1.3.5 - see https://issues.apache.org/jira/browse/WICKET-1777
	public static int AGGRESSIVE_CACHE_DURATION = (int) Duration.days(365).seconds();
	
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
	
	public void mountVersionedResource(String mountPrefix, ResourceReference ref) {
		mountVersionedResource(mountPrefix, ref, true);
	}
	
	public void mountVersionedResource(String mountPrefix, ResourceReference ref, boolean versionRequired) {
		mountPrefix = normalizeMountPrefix(mountPrefix);
		AbstractResourceVersion version;
		try {
			version = _versionProvider.getVersion(ref.getScope(), ref.getName());
		} catch (VersionException e) {
			if (versionRequired) {
				throw new WicketRuntimeException(e);
			} else {
				version = AbstractResourceVersion.NO_VERSION;
			}
		}
		_application.getSharedResources().add(ref.getName(), getResourceReference(ref.getScope(), ref.getName(), version.isValid())
				.getResource());
		mountSharedResourceWithCaching(getVersionedPath(ref.getScope(), ref.getName(), mountPrefix + ref.getName()), ref.getSharedResourceKey(), version.isValid());
	}

	private String normalizeMountPrefix(String mountPrefix) {
		return (mountPrefix.startsWith("/") ? "" : "/") + mountPrefix + (mountPrefix.endsWith("/") ? "" : "/");
	}
	
	protected void mountSharedResourceWithCaching(final String path, final String resourceKey, final boolean aggressiveCaching) {
		_application.mount(new SharedResourceRequestTargetUrlCodingStrategy(path, resourceKey) {

			@Override
			public IRequestTarget decode(final RequestParameters requestParameters) {
				final SharedResourceRequestTarget t = (SharedResourceRequestTarget) super
						.decode(requestParameters);
				if (t != null) {
					// wrap target
					return new IRequestTarget() {

						public void detach(final RequestCycle requestCycle) {
							t.detach(requestCycle);
						}

						public void respond(final RequestCycle requestCycle) {
							t.respond(requestCycle);
							final WebResponse response = (WebResponse) requestCycle.getResponse();
							response.setDateHeader("Expires", System.currentTimeMillis()
									+ getCacheDuration(aggressiveCaching) * 1000L);
							response.setHeader("Cache-Control", "max-age=" + getCacheDuration(aggressiveCaching));
						}
					};
				} else {
					return null;
				}

			}

		});
	}
	
	public ResourceReference[] mountResources(final Class<?> scope, final String mountPrefix, final boolean detectVersion, final String... files) {
		final ResourceReference[] references = new ResourceReference[files.length];
		for (int i = 0; i < files.length; i++) {
			final String file = files[i];
			references[i] = mountResource(scope, mountPrefix, detectVersion, file, file);
		}
		return references;
	}

	public ResourceReference mountResource(final Class<?> scope, final String mountPrefix, final boolean detectVersion, final String fileName) {
		return mountResource(scope, mountPrefix, detectVersion, fileName, fileName);
	}
	
	public ResourceReference mountResource(final Class<?> scope, final String mountPrefix, final boolean detectVersion, final String fileName, final String filePath) {
		final String unversionedPath = mountPrefix + filePath;
		final String versionedPath = mountPrefix
				+ (detectVersion ? getVersionedPath(scope, fileName, filePath) : filePath);

		final ResourceReference ref = getResourceReference(scope, fileName, !unversionedPath.equals(versionedPath));
		_application.mountSharedResource(versionedPath, ref.getSharedResourceKey());

		// redirect to orig page
		if (!unversionedPath.equals(versionedPath)) {
			_application.mount(new RedirectStrategy(unversionedPath, versionedPath));
		}

		return ref;
	}

	public ResourceReference[] mountCachedUnversionedResources(final Class<?> scope, final String mountPrefix, final String... files) {
		final ResourceReference[] references = new ResourceReference[files.length];
		for (int i = 0; i < files.length; i++) {
			final String file = files[i];
			references[i] = mountCachedUnversionedResource(scope, mountPrefix, file, file);
		}
		return references;
	}
	
	public ResourceReference mountCachedUnversionedResource(final Class<?> scope, final String mountPrefix, final String fileName) {
		return mountCachedUnversionedResource(scope, mountPrefix, fileName, fileName);
	}
	
	public ResourceReference mountCachedUnversionedResource(final Class<?> scope, final String mountPrefix, final String fileName, final String filePath) {
		String path = normalizeMountPrefix(mountPrefix) + filePath;
		final ResourceReference ref = getResourceReference(scope, fileName, true);
		_application.mountSharedResource(path, ref.getSharedResourceKey());
		return ref;
	}
	
	public static void mountWicketResources(String mountPrefix, WebApplication application) {
		ResourceMountHelper h = new ResourceMountHelper(application, new WicketVersionProvider(application));
		h.mountVersionedResource(mountPrefix, WicketAjaxReference.INSTANCE);
		h.mountVersionedResource(mountPrefix, WicketEventReference.INSTANCE);
	}
	
	public ResourceReference mountMergedSharedResource(final String mountPrefix, final String path, final boolean detectVersion, final Class<?>... scopes) {
		int idx = path.lastIndexOf('.');
		String suffix = idx > 0 ? path.substring(idx) : "";
		return mountMergedSharedResource(mountPrefix, path, suffix, detectVersion, scopes);
	}
	
	public ResourceReference mountMergedSharedResource(final String mountPrefix, final String path, String suffix, final boolean detectVersion, final Class<?>... scopes) {
		if (!suffix.equals("") && !suffix.startsWith(".")) {
			suffix = "." + suffix;
		}
		
		String[] files = new String[scopes.length];
		for (int i = 0; i < scopes.length; i++) {
			files[i] = scopes[i].getSimpleName() + suffix;
		}
		return mountMergedSharedResource(mountPrefix, path, detectVersion, scopes, files, true);
	}
	
	public ResourceReference mountMergedSharedResource(final String mountPrefix, final String path, final boolean detectVersion, final Class<?>[] scopes, final String[] files) {
		return mountMergedSharedResource(mountPrefix, path, detectVersion, scopes, files, true);
	}

	public ResourceReference mountMergedSharedResource(String mountPrefix, final String path, final boolean detectVersion, final Class<?>[] scopes, final String[] files, final boolean versionRequired) {

		if (scopes.length == 0 || files.length != scopes.length) {
			throw new IllegalArgumentException("arrays must not be empty and of equal size");
		}

		mountPrefix = normalizeMountPrefix(mountPrefix);
		
		final String unversionedPath = mountPrefix + path;
		String versionedPath = unversionedPath;
		AbstractResourceVersion version = AbstractResourceVersion.NO_VERSION;
		if (detectVersion) {
			for (int i = 0; i < scopes.length; i++) {
				AbstractResourceVersion v = getVersion(scopes[i], files[i], versionRequired);
				try {
					if (v.compareTo(version) > 0) {
						version = v;
					}
				} catch (IncompatibleVersionsException e) {
					throw new WicketRuntimeException(e);
				}
			}
			if (version.isValid()) {
				versionedPath = getVersionedPath(mountPrefix + path, version);
			}
		}

		final ResourceReference ref = getResourceReference(path, scopes, files, version.isValid());
		
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

		_application.mount(new MergedResourceRequestTargetUrlCodingStrategy(versionedPath, ref.getSharedResourceKey(), mergedKeys));

		// redirect to orig page
		if (!unversionedPath.equals(versionedPath)) {
			_application.mount(new RedirectStrategy(unversionedPath, versionedPath));
		}

		return ref;
	}

	private AbstractResourceVersion getVersion(Class<?> scope, String file, boolean versionRequired) {
		try {
			return _versionProvider.getVersion(scope, file);
		} catch (final VersionException e) {
			if (versionRequired) {
				throw new WicketRuntimeException(e);
			} else {
				return AbstractResourceVersion.NO_VERSION;
			}
		}
	}

	private ResourceReference getResourceReference(Class<?> scope, final String file, boolean aggressiveCaching) {
		final ResourceReference ref;
		int cacheDuration = getCacheDuration(aggressiveCaching);
		if (doCompress(file)) {
			if (isCSS(file)) {
				ref = new CachedCompressedCssResourceReference(scope, file, null, null, cacheDuration);
			} else if (isJS(file)) {
				ref = new CachedCompressedJsResourceReference(scope, file, null, null, cacheDuration);
			} else {
				ref = new CachedCompressedResourceReference(scope, file, null, null, cacheDuration);
			}
		} else {
			ref = new CachedResourceReference(scope, file, null, null, cacheDuration);
		}
		ref.bind(_application);
		return ref;
	}
	
	private ResourceReference getResourceReference(final String path,
			final Class<?>[] scopes, final String[] files, boolean aggressiveCaching) {
		final ResourceReference ref;
		
		int cacheDuration = getCacheDuration(aggressiveCaching);
		ResourceSpec[] specs = ResourceSpec.toResourceSpecs(scopes, files);
		
		if (doCompress(path)) {
			if (isCSS(path)) {
				ref = new CompressedMergedCssResourceReference(path, null, null, 
						specs, cacheDuration);
			} else if (isJS(path)) {
				ref = new CompressedMergedJsResourceReference(path, null, null, 
						specs, cacheDuration);
			} else {
				ref = new CompressedMergedResourceReference(path, null, null, 
						specs, cacheDuration);
			}
		} else {
			ref = new MergedResourceReference(path, null, null, specs, cacheDuration);
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

	protected String getVersionedPath(final String filePath, final AbstractResourceVersion version) {
		String versionString = version.isValid() ? "-" + version.getVersion() : "";
		
		return Strings.beforeLast(filePath, '.') + versionString + "."
				+ Strings.afterLast(filePath, '.');
	}

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

package org.wicketstuff.mergedresources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
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
import org.wicketstuff.mergedresources.util.Pair;
import org.wicketstuff.mergedresources.util.RedirectStrategy;
import org.wicketstuff.mergedresources.versioning.AbstractResourceVersion;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.WicketVersionProvider;
import org.wicketstuff.mergedresources.versioning.AbstractResourceVersion.IncompatibleVersionsException;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider.VersionException;

public class ResourceMount implements Cloneable {
	public static final int DEFAULT_CACHE_DURATION = (int) Duration.hours(1).seconds();
	public static final int DEFAULT_AGGRESIVE_CACHE_DURATION = (int) Duration.days(365).seconds();
	
	private static final Set<String> DEFAULT_COMPRESS_SUFFIXES = Collections
			.unmodifiableSet(new HashSet<String>(Arrays.asList("html", "css", "js", "xml")));
	private static final Set<String> DEFAULT_MERGE_SUFFIXES = Collections
		.unmodifiableSet(new HashSet<String>(Arrays.asList("css", "js")));
	
	private Integer _cacheDuration = DEFAULT_CACHE_DURATION;
	private String _path = null;
	private AbstractResourceVersion _version = null;
	private AbstractResourceVersion _minVersion = null;
	private boolean _requireVersion = true;
	private IResourceVersionProvider _resourceVersionProvider = null;
	private Boolean _compressed = null;
	private List<ResourceSpec> _resourceSpecs = new ArrayList<ResourceSpec>();
	private Set<String> _compressedSuffixes = new HashSet<String>(DEFAULT_COMPRESS_SUFFIXES);
	private Set<String> _mergedSuffixes = new HashSet<String>(DEFAULT_MERGE_SUFFIXES);
	private Locale _locale;
	private String _style;
	private Boolean _minifyJs;
	private Boolean _minifyCss;
	private boolean _mountRedirect = true;
	private Class<?> _mountScope;
	private Boolean _merge;
	
	public static void mountWicketResources(String mountPrefix, WebApplication application) {
		ResourceMount mount = new ResourceMount()
			.setResourceVersionProvider(new WicketVersionProvider(application))
			.setDefaultAggresiveCacheDuration();
		
		if (!mountPrefix.endsWith("/")) {
			mountPrefix = mountPrefix + "/";
		}
		
		for (ResourceReference ref : new ResourceReference[]{WicketAjaxReference.INSTANCE, WicketEventReference.INSTANCE}) {
			String path = mountPrefix + ref.getName();
			
			mount
				.clone()
				.setPath(path)
				.addResourceSpec(ref)
				.mount(application);
		}
	}
	
	public ResourceMount() {

	}

	public ResourceMount setCompressed(boolean compressed) {
		_compressed = compressed;
		return this;
	}
	
	public ResourceMount autodetectCompression() {
		_compressed = null;
		return this;
	}
	
	public ResourceMount setMerged(boolean merge) {
		_merge = merge;
		return this;
	}
	
	
	public ResourceMount autodetectMerging() {
		_merge = null;
		return this;
	}


	public ResourceMount setVersion(AbstractResourceVersion version) {
		_version = version;
		return this;
	}

	public ResourceMount setMinVersion(AbstractResourceVersion minVersion) {
		_minVersion = minVersion;
		return this;
	}
	
	public ResourceMount unsetMinVersion() {
		_minVersion = null;
		return this;
	}
	
	public ResourceMount setRequireVersion(boolean requireVersion) {
		_requireVersion = requireVersion;
		return this;
	}
	
	public ResourceMount setNoVersion() {
		return setVersion(AbstractResourceVersion.NO_VERSION);
	}
	
	public ResourceMount autodetectVersion() {
		_version = null;
		return this;
	}

	public ResourceMount setPath(String path) {
		if (path != null) {
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
		}
		_path = path;
		return this;
	}

	public ResourceMount setPath(String prefix, ResourceReference ref) {
		if (!prefix.endsWith("/")) {
			prefix = prefix + "/";
		}
		return setPath(prefix + ref.getName());
	}
	
	public ResourceMount setPath(String prefix, ResourceReference ref, String suffix) {
		if (!prefix.endsWith("/")) {
			prefix = prefix + "/";
		}
		if (suffix == null) {
			suffix = "";
		}
		return setPath(prefix + ref.getName() + suffix);
	}
	
	public ResourceMount setMountRedirect(boolean mountRedirect) {
		_mountRedirect = mountRedirect;
		return this;
	}

	public ResourceMount setLocale(Locale locale) {
		_locale = locale;
		return this;
	}

	public ResourceMount setStyle(String style) {
		_style = style;
		return this;
	}

	public ResourceMount setCacheDuration(int cacheDuration) {
		_cacheDuration = cacheDuration;
		return this;
	}

	public ResourceMount setDefaultAggresiveCacheDuration() {
		return setCacheDuration(DEFAULT_AGGRESIVE_CACHE_DURATION);
	}
	
	public ResourceMount setResourceVersionProvider(IResourceVersionProvider resourceVersionProvider) {
		_resourceVersionProvider = resourceVersionProvider;
		return this;
	}

	public ResourceMount setMinifyJs(Boolean minifyJs) {
		_minifyJs = minifyJs;
		return this;
	}

	public ResourceMount autodetectMinifyJs() {
		_minifyJs = null;
		return this;
	}

	public ResourceMount setMinifyCss(Boolean minifyCss) {
		_minifyCss = minifyCss;
		return this;
	}

	public ResourceMount autodetectMinifyCss() {
		_minifyCss = null;
		return this;
	}

	public ResourceMount setMountScope(Class<?> mountScope) {
		_mountScope = mountScope;
		return this;
	}

	public ResourceMount autodetectMountScope() {
		return setMountScope(null);
	}

	public ResourceMount addResourceSpec(ResourceSpec resourceSpec) {
		if (_resourceSpecs.contains(resourceSpec)) {
			throw new IllegalArgumentException("aleady added: " + resourceSpec);
		}
		_resourceSpecs.add(resourceSpec);
		return this;
	}

	public ResourceMount addResourceSpec(Class<?> scope, String name) {
		return addResourceSpec(new ResourceSpec(scope, name));
	}
	
	public ResourceMount addResourceSpecs(Class<?> scope, String ... names) {
		for (String name : names) {
			addResourceSpec(new ResourceSpec(scope, name));
		}		
		return this;
	}
	
	public ResourceMount addResourceSpec(Class<?> scope, String name, Locale locale, String style, Integer cacheDuration) {
		return addResourceSpec(new ResourceSpec(scope, name, locale, style, cacheDuration));
	}
	
	public ResourceMount addResourceSpecs(ResourceSpec... resourceSpecs) {
		return addResourceSpecs(Arrays.asList(resourceSpecs));
	}

	public ResourceMount addResourceSpecs(Iterable<ResourceSpec> resourceSpecs) {
		for (ResourceSpec resourceSpec : resourceSpecs) {
			addResourceSpec(resourceSpec);
		}
		return this;
	}

	public ResourceMount addResourceSpecMatchingSuffix(Class<?> scope, String suffix) {
		if (!suffix.startsWith(".")) {
			suffix = "." + suffix;
		}
		return addResourceSpec(new ResourceSpec(scope, scope.getSimpleName() + suffix));
	}

	public ResourceMount addResourceSpecsMatchingSuffixes(Class<?> scope, String... suffixes) {
		return addResourceSpecsMatchingSuffix(scope, Arrays.asList(suffixes));
	}

	public ResourceMount addResourceSpecsMatchingSuffix(Class<?> scope, Iterable<String> suffixes) {
		for (String suffix : suffixes) {
			addResourceSpecMatchingSuffix(scope, suffix);
		}
		return this;
	}

	public ResourceMount addResourceSpecsMatchingSuffix(Class<?> ... scopes) {
		if (_path == null) {
			throw new IllegalStateException("unversionPath must be set for this method to work");
		}
		String suffix = Strings.afterLast(_path, '.');
		if (Strings.isEmpty(suffix) || suffix.contains("/")) {
			throw new IllegalStateException("unversionPath does not have a valid suffix (i.e. does not contain a '.' followed by characterers and no '/')");
		}
		for (Class<?> scope : scopes) {
			addResourceSpecMatchingSuffix(scope, suffix);
		}
		return this;
	}
	
	public ResourceMount addResourceSpec(ResourceReference ref) {
		return addResourceSpec(new ResourceSpec(ref));
	}
	
	public ResourceMount addResourceSpecs(ResourceReference ... refs) {
		for (ResourceReference ref : refs) {
			addResourceSpec(ref);
		}
		return this;
	}
	
	public ResourceMount mount(WebApplication application) {
		if (_resourceSpecs.size() == 0) {
			throw new IllegalStateException("no ResourceSpecs to mount");
		}
		try {
			List<Pair<String, ResourceSpec[]>> specsList;

			boolean merge = doMerge();
			if (merge) {
				specsList = new ArrayList<Pair<String,ResourceSpec[]>>(1);
				specsList.add(new Pair<String, ResourceSpec[]>(null, getResourceSpecs()));
			} else {
				specsList = new ArrayList<Pair<String,ResourceSpec[]>>(_resourceSpecs.size());
				for (ResourceSpec spec : _resourceSpecs) {
					specsList.add(new Pair<String, ResourceSpec[]>(_resourceSpecs.size() > 1 ? spec.getFile() : null, getResourceSpecs()));
				}
			} 
			
			for (Pair<String, ResourceSpec[]> p : specsList) {
				String path = getPath(p.getFirst(), true);
				String unversionedPath = getPath(p.getFirst(), false);
				
				ResourceSpec[] specs = p.getSecond();
				
				final ResourceReference ref = newResourceReference(getScope(), path, getLocale(), getStyle(), getCacheDuration(), specs);
				ref.bind(application);
	//			application.getSharedResources()
	//					.add(ref.getScope(), ref.getName(), ref.getLocale(), ref.getStyle(), ref.getResource());
	
				application.mount(newStrategy(path, ref, merge));
	
				if (_mountRedirect && !unversionedPath.equals(path)) {
					application.mount(newRedirectStrategy(unversionedPath, path));
				}
			}
			return this;
		} catch (Exception e) {
			throw new WicketRuntimeException("failed to mount resource ('" + _path
					+ "')", e);
		}
	}

	protected IRequestTargetUrlCodingStrategy newStrategy(String mountPath, final ResourceReference ref, boolean merge) {
		if (merge) {
			final ArrayList<String> mergedKeys = new ArrayList<String>(_resourceSpecs.size());
			for (ResourceSpec spec : _resourceSpecs) {
				mergedKeys.add(new ResourceReference(spec.getScope(), spec.getFile()) {

					private static final long serialVersionUID = 1L;

					@Override
					protected Resource newResource() {
						return ref.getResource();
					}

				}.getSharedResourceKey());
			}
			return new MergedResourceRequestTargetUrlCodingStrategy(mountPath, ref.getSharedResourceKey(), mergedKeys);
		} else {
			return new SharedResourceRequestTargetUrlCodingStrategy(mountPath, ref.getSharedResourceKey());
		}
	}

	protected IRequestTargetUrlCodingStrategy newRedirectStrategy(String mountPath, String redirectPath) {
		return new RedirectStrategy(mountPath, redirectPath);
	}

	public String getPath() throws VersionException, IncompatibleVersionsException {
		return getPath(null, true);
	}
	
	public String getPath(String appendName) throws VersionException, IncompatibleVersionsException {
		return getPath(appendName, true);
	}
	
	public String getPath(String appendName, boolean appendVersion) throws VersionException, IncompatibleVersionsException {
		if (_path == null) {
			throw new IllegalStateException("path must be set");
		}

		String path = _path;
		if (appendName != null) {
			if (!path.endsWith("/")) {
				path = path + "/";
			}
			path = path + appendName;
		}
		
		AbstractResourceVersion version = getVersion();

		if (appendVersion && version != null && version.isValid()) {
			String versionString = "-" + version.getVersion();

			return Strings.beforeLast(path, '.') + versionString + "."
					+ Strings.afterLast(path, '.');
		}

		return _path;
	}

	protected AbstractResourceVersion getVersion() throws VersionException, IncompatibleVersionsException {
		if (_version != null) {
			return _version;
		}

		if (_resourceVersionProvider != null) {
			AbstractResourceVersion max = _minVersion;
			for (ResourceSpec spec : _resourceSpecs) {
				try {
					AbstractResourceVersion version = _resourceVersionProvider.getVersion(spec
							.getScope(), spec.getFile());
					if (max == null || version.compareTo(max) > 0) {
						max = version;
					}
				} catch (VersionException e) {
					if (_requireVersion) {
						throw e;
					}
				}
			}
			return max;
		}

		return null;
	}

	protected Class<?> getScope() {
		if (_mountScope != null) {
			return _mountScope;
		} else {
			Class<?> scope = null;
			for (ResourceSpec resourceSpec : _resourceSpecs) {
				if (scope == null) {
					scope = resourceSpec.getScope();
				} else if (!scope.equals(resourceSpec.getScope())) {
					scope = null;
					break;
				}
			}
			if (scope != null) {
				return scope;
			}
		}
		
		return ResourceMount.class;
	}

	protected ResourceReference newResourceReference(Class<?> scope, final String path, Locale locale, String style, int cacheDuration, ResourceSpec[] resourceSpecs) {
		ResourceReference ref;
		if (resourceSpecs.length > 1) {
			if (doCompress(path)) {
				if (doMinifyJs(path)) {
					ref = new CompressedMergedCssResourceReference(path, locale, style, resourceSpecs, cacheDuration);
				} else if (doMinifyJs(path)) {
					ref = new CompressedMergedJsResourceReference(path, locale, style, resourceSpecs, cacheDuration);
				} else {
					ref = new CompressedMergedResourceReference(path, locale, style, resourceSpecs, cacheDuration);
				}
			} else {
				ref = new MergedResourceReference(path, locale, style, resourceSpecs, cacheDuration);
			}
		} else if (resourceSpecs.length == 1) {
			ResourceSpec resourceSpec = resourceSpecs[0];
			if (resourceSpec.getRef() != null) {
				// reuse existing, e.g. for mountWicketResources(...)
				return resourceSpec.getRef();
			}
			
			if (doCompress(path)) {
				if (doMinifyCss(path)) {
					ref = new CachedCompressedCssResourceReference(scope, path, locale, style, cacheDuration);
				} else if (doMinifyJs(path)) {
					ref = new CachedCompressedJsResourceReference(scope, path, locale, style, cacheDuration);
				} else {
					ref = new CachedCompressedResourceReference(scope, path, locale, style, cacheDuration);
				}
			} else {
				ref = new CachedResourceReference(scope, path, locale, style, cacheDuration);
			}
		} else {
			throw new IllegalArgumentException("can't create ResourceReference without ResourceSpec");
		}
		return ref;
	}

	protected Locale getLocale() {
		if (_locale != null) {
			return _locale;
		}
		
		if (_resourceSpecs.size() == 0) {
			return null;
		}
		
		Locale locale = null;
		for (ResourceSpec spec : _resourceSpecs) {
			if (locale != null) {
				Locale newLocale = locale;
				if (spec.getLocale() != null) {
					if (spec.getLocale().getLanguage() != null) {
						newLocale = locale;
						if (locale.getLanguage() != null && !spec.getLocale().getLanguage().equals(locale.getLanguage())) {
							throw new IllegalStateException("languages aren't compatible: '" + locale + "' and '" + spec.getLocale() + "'");
						}
					}
					
					if (spec.getLocale().getCountry() != null) {
						if (locale.getCountry() != null && !spec.getLocale().getCountry().equals(locale.getCountry())) {
							throw new IllegalStateException("countries aren't compatible: '" + locale + "' and '" + spec.getLocale() + "'");
						}
					} else if (locale.getCountry() != null) {
						// keep old locale, as it is more restrictive
						newLocale = locale;
					}					
				}
				locale = newLocale;
			} else {
				locale = spec.getLocale();
			}
		}
		return locale;
	}
	
	protected String getStyle() {
		if (_style != null) {
			return _style;
		}
		
		if (_resourceSpecs.size() == 0) {
			return null;
		}
		
		String style = null;
		for (ResourceSpec spec : _resourceSpecs) {
			if (style != null) {
				if (spec.getStyle() != null && !spec.getStyle().equals(style)) {
					throw new IllegalStateException("styles aren't compatible: '" + style + "' and '" + spec.getStyle() + "'");
				}
			} else {
				style = spec.getStyle();
			}
		}
		return style;
	}
	
	protected int getCacheDuration() {
		if (_cacheDuration != null) {
			return _cacheDuration;
		}
		
		int cacheDuration = DEFAULT_CACHE_DURATION;
		for (ResourceSpec spec : _resourceSpecs) {
			if (spec.getCacheDuration() != null && spec.getCacheDuration() < cacheDuration) {
				cacheDuration = spec.getCacheDuration();
			}
		}
		return cacheDuration;
	}
	
	protected ResourceSpec[] getResourceSpecs() {
		return _resourceSpecs.toArray(new ResourceSpec[_resourceSpecs.size()]);
	}

	protected boolean doCompress(final String file) {
		return _compressed == null ? _compressedSuffixes.contains(Strings.afterLast(file, '.')) : _compressed;
	}

	protected boolean doMinifyJs(final String file) {
		return _minifyJs == null ? file.endsWith(".js") : _minifyJs;
	}

	protected boolean doMinifyCss(final String file) {
		return _minifyCss == null ? file.endsWith(".css") : _minifyCss;
	}

	protected boolean doMerge() {
		return _merge == null ? _resourceSpecs.size() > 1 && _mergedSuffixes.contains(Strings.afterLast(_path, '.')) : _merge;
	}
	
	public ResourceMount clearSpecs() {
		_resourceSpecs.clear();
		return this;
	}

	@Override
	public ResourceMount clone() {
		try {
			ResourceMount clone = (ResourceMount) super.clone();
			// copy collections
			clone._compressedSuffixes = new HashSet<String>(_compressedSuffixes);
			clone._mergedSuffixes = new HashSet<String>(_mergedSuffixes);
			clone._resourceSpecs = new ArrayList<ResourceSpec>(_resourceSpecs);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new WicketRuntimeException("clone of Object not supported?", e);
		}
	}
	
	
}

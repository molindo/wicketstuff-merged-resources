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
	
	/**
	 * file suffixes to be compressed by default ("css", "js", "html", "xml"). For instance, there is no sense in 
	 * gzipping images
	 */
	public static final Set<String> DEFAULT_COMPRESS_SUFFIXES = Collections
			.unmodifiableSet(new HashSet<String>(Arrays.asList("html", "css", "js", "xml")));
	
	/**
	 * file suffixes to be merged by default ("css" and "js"). For instance, there is no sense in merging
	 * xml files into a single one by default (you don't want multiple root elements)
	 */
	public static final Set<String> DEFAULT_MERGE_SUFFIXES = Collections
		.unmodifiableSet(new HashSet<String>(Arrays.asList("css", "js")));
	
	private Integer _cacheDuration = null;
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
	
	/**
	 * Mount wicket-event.js and wicket-ajax.js using wicket's version for aggressive caching (e.g. wicket-ajax-1.3.6.js)
	 * 
	 * @param mountPrefix e.g. "script" for "/script/wicket-ajax-1.3.6.js
	 * @param application the application
	 */
	public static void mountWicketResources(String mountPrefix, WebApplication application) {
		mountWicketResources(mountPrefix, application, new ResourceMount().setDefaultAggresiveCacheDuration());
	}
	
	/**
	 * Mount wicket-event.js and wicket-ajax.js using wicket's version (e.g. wicket-ajax-1.3.6.js). 
	 * 
	 * @param mountPrefix e.g. "script" for "/script/wicket-ajax-1.3.6.js
	 * @param application the application
	 * @param mount pre-configured resource mount to use. ResourceVersionProvider will be overriden
	 */
	public static void mountWicketResources(String mountPrefix, WebApplication application, ResourceMount mount) {
		mount = mount.clone()
			.setResourceVersionProvider(new WicketVersionProvider(application));
		
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
	
	/**
	 * Create a new ResourceMount with default settings
	 */
	public ResourceMount() {

	}

	/**
	 * @param compressed whether this resources should be compressed. default is autodetect
	 * @return this
	 * @see ResourceMount#autodetectCompression()
	 */
	public ResourceMount setCompressed(boolean compressed) {
		_compressed = compressed;
		return this;
	}
	
	/**
	 * autodetect whether this resource should be compressed using suffix of file name (e.g. ".css") Behavior might be overriden in {@link #doCompress(String)}
	 * @return this
	 * @see ResourceMount#setCompressed(boolean)
	 */
	public ResourceMount autodetectCompression() {
		_compressed = null;
		return this;
	}
	
	/**
	 * @param merge whether all {@link ResourceSpec}s should be merged to a single resource. default is autodetect
	 * @return this
	 * @see ResourceMount#autodetectMerging()
	 */
	public ResourceMount setMerged(boolean merge) {
		_merge = merge;
		return this;
	}
	
	/**
	 * autodetect whether this resource should be merged using suffix of file name (e.g. ".js")
	 * @return this
	 * @see #setMerged(boolean)
	 */
	public ResourceMount autodetectMerging() {
		_merge = null;
		return this;
	}

	/**
	 * force a resource version, any {@link IResourceVersionProvider} ({@link #setResourceVersionProvider(IResourceVersionProvider)}) will be ignored. default is <code>null</code>
	 * @param version version
	 * @return this
	 */
	public ResourceMount setVersion(AbstractResourceVersion version) {
		_version = version;
		return this;
	}

	/**
	 * same as passing {@link AbstractResourceVersion#NO_VERSION} to {@link #setVersion(AbstractResourceVersion)}
	 * @return this
	 * @see #setVersion(AbstractResourceVersion)
	 */
	public ResourceMount setNoVersion() {
		return setVersion(AbstractResourceVersion.NO_VERSION);
	}
	
	/**
	 * same as passing <code>null</code> to  {@link #setVersion(AbstractResourceVersion)}
	 * @return this
	 * @see #setVersion(AbstractResourceVersion)
	 */
	public ResourceMount autodetectVersion() {
		return setVersion(null);
	}
	
	/**
	 * force a minimal version. default is <code>null</code>
	 * @param minVersion
	 * @return this
	 */
	public ResourceMount setMinVersion(AbstractResourceVersion minVersion) {
		_minVersion = minVersion;
		return this;
	}
	
	/**
	 * unset minimal version, same as passing <code>null</code> to {@link #setMinVersion(AbstractResourceVersion)}
	 * @return this
	 */
	public ResourceMount unsetMinVersion() {
		return setMinVersion(null);
	}
	
	/**
	 * {@link IResourceVersionProvider} might not always be able to detect the version of a resource. This might be ignored or cause an error depending.
	 * default is to cause an error (<code>true</code>)
	 * @param requireVersion whether version is required (<code>true</code>) or not (<code>false</code>). default is <code>true</code>
	 * @return this
	 */
	public ResourceMount setRequireVersion(boolean requireVersion) {
		_requireVersion = requireVersion;
		return this;
	}

	/**
	 * the path to user for mounting. this might either be a prefix if multiple resources are mounted or the full name. if used as prefix,
	 * {@link ResourceSpec#getFile()} is appended
	 * @param path name or prefix for mount, with or without leading or trailing slashes
	 * @return this
	 */
	public ResourceMount setPath(String path) {	
		if (path != null) {
			path = path.trim();
			if ("".equals(path) || "/".equals(path)) {
				throw new IllegalArgumentException("path must not be empty or '/', was " + path);
			}
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
		}
		_path = path;
		return this;
	}

	/**
	 * convenience method to use {@link #setPath(String)} use a prefix and {@link ResourceReference#getName()}.
	 * @param prefix path prefix prefix for mount, with or without leading or trailing slashes
	 * @param ref a {@link ResourceReference}
	 * @return this
	 */
	public ResourceMount setPath(String prefix, ResourceReference ref) {
		if (!prefix.endsWith("/")) {
			prefix = prefix + "/";
		}
		return setPath(prefix + ref.getName());
	}
	
	/**
	 * convenience method to use {@link #setPath(String)} use a prefix and {@link ResourceReference#getName()}.
	 * @param prefix path prefix prefix for mount, with or without leading or trailing slashes
	 * @param ref a {@link ResourceReference}
	 * @param suffix suffix to append after {@link ResourceReference#getName()}, might be null
	 * @return this
	 */
	public ResourceMount setPath(String prefix, ResourceReference ref, String suffix) {
		return setPath(prefix, ref.getName(), suffix);
	}
	
	/**
	 * convenience method to use {@link #setPath(String)} use a prefix and a name
	 * @param prefix path prefix prefix for mount, with or without leading or trailing slashes
	 * @param name a name
	 * @param suffix suffix to append after {@link ResourceReference#getName()}, might be null
	 * @return this
	 */
	public ResourceMount setPath(String prefix, String name, String suffix) {
		if (!prefix.endsWith("/")) {
			prefix = prefix + "/";
		}
		if (Strings.isEmpty(suffix)) {
			suffix = "";
		} else if (!suffix.startsWith(".")) {
			suffix = "." + suffix;
		}
		return setPath(prefix + name + suffix);
	}
	
	/**
	 * @param mountRedirect whether a redirected should be mounted from the unversioned path to the versioned path (only used if there is a version). default is <code>true</code>
	 * @return this
	 */
	public ResourceMount setMountRedirect(boolean mountRedirect) {
		_mountRedirect = mountRedirect;
		return this;
	}

	/**
	 * Locale might either be detected from added {@link ResourceSpec}s or set manually.
	 * @param locale Locale for mounted resources
	 * @return this
	 * @see {@link ResourceReference#setLocale(Locale)}
	 */
	public ResourceMount setLocale(Locale locale) {
		_locale = locale;
		return this;
	}
	
	/**
	 * Autodetect the locale. Same as passing <code>null</code> to {@link #setLocale(Locale)}
	 * @return this
	 */
	public ResourceMount autodetectLocale() {
		return setLocale(null);
	}
	
	/**
	 * Style might either be detected from added {@link ResourceSpec}s or set manually.
	 * @param style Style for mounted resources
	 * @return this
	 * @see {@link ResourceReference#setStyle(String)}
	 */
	public ResourceMount setStyle(String style) {
		_style = style;
		return this;
	}

	/**
	 * Autodetect the style. Same as passing <code>null</code> to {@link #setStyle(String)}
	 * @return this
	 */
	public ResourceMount autodetectStyle() {
		return setStyle(null);
	}
	
	/**
	 * Set cache duration in seconds. default is autodetect ({@link <code>null</code>}). Must be >= 0
	 * @param cacheDuration
	 * @return this
	 * @see #autodetectCacheDuration()
	 */
	public ResourceMount setCacheDuration(int cacheDuration) {
		if (cacheDuration < 0) {
			throw new IllegalArgumentException("cacheDuration must not be < 0, was " + cacheDuration);
		}
		_cacheDuration = cacheDuration;
		return this;
	}

	/**
	 * Same as passing {@link ResourceMount#DEFAULT_CACHE_DURATION} to {@link #setCacheDuration(int)}
	 * @return this
	 */
	public ResourceMount setDefaultCacheDuration() {
		return setCacheDuration(DEFAULT_CACHE_DURATION);
	}
	
	/**
	 * Same as passing {@link ResourceMount#DEFAULT_AGGRESIVE_CACHE_DURATION} to {@link #setCacheDuration(int)}
	 * @return this
	 */
	public ResourceMount setDefaultAggresiveCacheDuration() {
		return setCacheDuration(DEFAULT_AGGRESIVE_CACHE_DURATION);
	}
	
	/**
	 * autodetect cache duration: use minimum of all resource specs or {@link ResourceMount#DEFAULT_CACHE_DURATION} if 
	 * not available. Behavior might be overriden using {@link #getCacheDuration()}
	 * @return this
	 */
	public ResourceMount autodetectCacheDuration() {
		_cacheDuration = null;
		return this;
	}
	
	/**
	 * Set the {@link IResourceVersionProvider} to use for {@link AbstractResourceVersion} detection
	 * @param resourceVersionProvider the resource version provider
	 * @return this
	 */
	public ResourceMount setResourceVersionProvider(IResourceVersionProvider resourceVersionProvider) {
		_resourceVersionProvider = resourceVersionProvider;
		return this;
	}

	/**
	 * @param minifyJs whether js should be minified (<code>true</code>) or not (<code>false</code>). Default is
	 * autodetect
	 * @return this
	 * @see #autodetectMinifyJs()
	 */
	public ResourceMount setMinifyJs(Boolean minifyJs) {
		_minifyJs = minifyJs;
		return this;
	}

	/**
	 * Autodetect wheter resource should be minified using a JS compressor. Default is to minify files ending with .js.
	 * Behavior might be overriden using {@link #doMinifyJs(String)}
	 * @return this
	 */
	public ResourceMount autodetectMinifyJs() {
		_minifyJs = null;
		return this;
	}

	/**
	 * @param minifyCss whether css should be minified (<code>true</code>) or not (<code>false</code>). Default is autodetect
	 * @return this
	 * @see #autodetectMinifyCss()
	 */
	public ResourceMount setMinifyCss(Boolean minifyCss) {
		_minifyCss = minifyCss;
		return this;
	}

	/**
	 * Autodetect wheter resource should be minified using a CSS compressor. Default is to minify files ending with .css.
	 * Behavior might be overriden using {@link #doMinifyCss(String)}
	 * @return this
	 */
	public ResourceMount autodetectMinifyCss() {
		_minifyCss = null;
		return this;
	}

	/**
	 * The mount scope to use. default is autodetect (<code>null</code>)
	 * @param mountScope mount scope
	 * @return this
	 * @see ResourceReference#getScope()
	 * @see #autodetectMountScope()
	 */
	public ResourceMount setMountScope(Class<?> mountScope) {
		_mountScope = mountScope;
		return this;
	}

	/**
	 * Same as passing <code>null</code> to {@link #setMountScope(Class)}. Autodetect: either use the scope that all
	 * (merged) resources are using or use {@link ResourceMount} as mount scope.
	 * @return
	 */
	public ResourceMount autodetectMountScope() {
		return setMountScope(null);
	}

	/**
	 * @param resourceSpec add a new {@link ResourceSpec}
	 * @return this
	 */
	public ResourceMount addResourceSpec(ResourceSpec resourceSpec) {
		if (_resourceSpecs.contains(resourceSpec)) {
			throw new IllegalArgumentException("aleady added: " + resourceSpec);
		}
		_resourceSpecs.add(resourceSpec);
		return this;
	}

	/**
	 * add a new {@link ResourceSpec} with this scope and name
	 * @param scope scope
	 * @param name name
	 * @return this
	 */
	public ResourceMount addResourceSpec(Class<?> scope, String name) {
		return addResourceSpec(new ResourceSpec(scope, name));
	}
	
	/**
	 * add a new {@link ResourceSpec} with this scope and each name
	 * @param scope scope
	 * @param names names
	 * @return this
	 */
	public ResourceMount addResourceSpecs(Class<?> scope, String ... names) {
		for (String name : names) {
			addResourceSpec(new ResourceSpec(scope, name));
		}		
		return this;
	}
	
	/**
	 * add a new {@link ResourceSpec} with this scope, name, locale, style and cacheDuration
	 * @param scope scope
	 * @param name name
	 * @param locale locale
	 * @param style style
	 * @param cacheDuration cache duration
	 * @return this
	 */
	public ResourceMount addResourceSpec(Class<?> scope, String name, Locale locale, String style, Integer cacheDuration) {
		return addResourceSpec(new ResourceSpec(scope, name, locale, style, cacheDuration));
	}
	
	/**
	 * add all resource specs
	 * @param resourceSpecs array of {@link ResourceSpec}s to add
	 * @return this
	 */
	public ResourceMount addResourceSpecs(ResourceSpec... resourceSpecs) {
		return addResourceSpecs(Arrays.asList(resourceSpecs));
	}

	/**
	 * add all resource specs
	 * @param resourceSpecs {@link Iterable} of {@link ResourceSpec}s to add
	 * @return this
	 */
	public ResourceMount addResourceSpecs(Iterable<ResourceSpec> resourceSpecs) {
		for (ResourceSpec resourceSpec : resourceSpecs) {
			addResourceSpec(resourceSpec);
		}
		return this;
	}

	/**
	 * Adds a resource spec for a resource with the same name as the scope, adding a suffix.
	 * Example: if scope is Foo.class and suffix is "js", name will be "Foo.js"
	 * @param scope the scope
	 * @param suffix the suffix
	 * @return this
	 */
	public ResourceMount addResourceSpecMatchingSuffix(Class<?> scope, String suffix) {
		if (!suffix.startsWith(".")) {
			suffix = "." + suffix;
		}
		return addResourceSpec(new ResourceSpec(scope, scope.getSimpleName() + suffix));
	}

	/**
	 * same as {@link #addResourceSpecMatchingSuffix(Class, String)} but using multiple suffixes
	 * @param scope the scope
	 * @param suffixes the suffixes
	 * @return this
	 */
	public ResourceMount addResourceSpecsMatchingSuffixes(Class<?> scope, String... suffixes) {
		return addResourceSpecsMatchingSuffix(scope, Arrays.asList(suffixes));
	}

	/**
	 * same as {@link #addResourceSpecMatchingSuffix(Class, String)} but using multiple suffixes
	 * @param scope the scope
	 * @param suffixes the suffixes
	 * @return this
	 */
	public ResourceMount addResourceSpecsMatchingSuffix(Class<?> scope, Iterable<String> suffixes) {
		for (String suffix : suffixes) {
			addResourceSpecMatchingSuffix(scope, suffix);
		}
		return this;
	}

	/**
	 * uses the path (set by {@link #setPath(String)}) to obtain a suffix to use with {@link #addResourceSpecMatchingSuffix(Class, String)}
	 * @param scopes
	 * @return this
	 */
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
	
	/**
	 * add a {@link ResourceSpec} using a {@link ResourceReference}
	 * @param ref the {@link ResourceReference}
	 * @return this
	 */
	public ResourceMount addResourceSpec(ResourceReference ref) {
		return addResourceSpec(new ResourceSpec(ref));
	}
	
	/**
	 * add a {@link ResourceSpec} for each {@link ResourceReference}
	 * @param refs the {@link ResourceReference}s
	 * @return this
	 */
	public ResourceMount addResourceSpecs(ResourceReference ... refs) {
		for (ResourceReference ref : refs) {
			addResourceSpec(ref);
		}
		return this;
	}
	
	/**
	 * mount the {@link ResourceSpec}(s) added either as a single {@link Resource} or multiple Resource, depending on
	 * {@link #doMerge()}. Might also mount a redirect for versioned path names. (e.g. from "/script/wicket-ajax.js" to "/script/wicket-ajax-1.3.6.js")
	 * 
	 * @param application the application
	 * @return this
	 */
	public ResourceMount mount(WebApplication application) {
		if (_resourceSpecs.size() == 0) {
			// nothing to do
			return this;
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
					specsList.add(new Pair<String, ResourceSpec[]>(_resourceSpecs.size() > 1 ? spec.getFile() : null, new ResourceSpec[]{spec}));
				}
			} 
			
			for (Pair<String, ResourceSpec[]> p : specsList) {
				ResourceSpec[] specs = p.getSecond();
				
				String path = getPath(p.getFirst(), specs);
				String unversionedPath = getPath(p.getFirst(), null);
				
				String name = specs.length == 1 ? specs[0].getFile() : unversionedPath;
				
				final ResourceReference ref = newResourceReference(getScope(specs), name, getLocale(specs), getStyle(specs), getCacheDuration(specs), specs);
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

	/**
	 * create a new {@link IRequestTargetUrlCodingStrategy}
	 * 
	 * @param mountPath the mount path
	 * @param ref the {@link ResourceReference}
	 * @param merge if <code>true</code>, all resources obtained by {@link #getResourceSpecs()} should be merged
	 * @return this
	 */
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

	/**
	 * create a new {@link IRequestTargetUrlCodingStrategy} to redirect from mountPath to redirectPath
	 * @param mountPath the path to redirect from
	 * @param redirectPath the path to redirect to
	 * @return a new {@link IRequestTargetUrlCodingStrategy}
	 */
	protected IRequestTargetUrlCodingStrategy newRedirectStrategy(String mountPath, String redirectPath) {
		return new RedirectStrategy(mountPath, redirectPath);
	}

	/**
	 * @return the path, same as passing <code>null</code> and <code>null</code> to {@link #getPath(String, ResourceSpec[])}
	 * @throws VersionException if version can't be found
	 * @throws IncompatibleVersionsException if versions can't be compared
	 * @see #getPath(String, boolean)
	 */
	public final String getPath() throws VersionException, IncompatibleVersionsException {
		return getPath(null, null);
	}
	
	/**
	 * @param appendName
	 * @return the path, same as passing <code>appendName</code> and <code>null</code> to {@link #getPath(String, ResourceSpec[])}
	 * @throws VersionException if version can't be found
	 * @throws IncompatibleVersionsException if versions can't be compared
	 * @see #getPath(String, boolean)
	 */
	public final String getPath(String appendName) throws VersionException, IncompatibleVersionsException {
		return getPath(appendName, null);
	}
	
	/**
	 * @param appendName the name to append after path
	 * @param specs a list of specs to get the version from or null
	 * @return the path
	 * @throws VersionException if version can't be found
	 * @throws IncompatibleVersionsException if versions can't be compared
	 */
	public String getPath(String appendName, ResourceSpec[] specs) throws VersionException, IncompatibleVersionsException {
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
		
		
		if (specs != null && specs.length > 0) {
			AbstractResourceVersion version = getVersion(specs);
			if (version != null && version.isValid()) {
				return buildVersionedPath(path, version);
			}
		}
		
		return path;
	}

	/**
	 * create a versioned path out of the given path and the version. default is to append the
	 * version after a '-' in front of the last '.' in the path. (e.g. wicket-ajax-1.3.6.js)
	 * if there is no '.' in the path or only at the beginning, a '-' and the version will be appended
	 * (e.g. foobar-1.3.6 or .something-1.3.6
	 * @param path the path
	 * @param version the version. must not be null but may be invalid (check version.isValid()!)
	 * @return the versioned path
	 */
	protected String buildVersionedPath(String path, AbstractResourceVersion version) {
		if (!version.isValid()) {
			return path;
		}
		int idx = path.lastIndexOf('.');
		if (idx > 0) {
			return path.substring(0, idx) + "-" + version.getVersion() + path.substring(idx);
		} else {
			return path + "-" + version.getVersion();
		}
	}

	/**
	 * detect the version. default implementation is to use the manually set version or detect it using
	 * {@link IResourceVersionProvider} from all specs. 
	 * @param specs the specs to detect the version from
	 * @return the version
	 * @throws VersionException If a version can't be determined from any resource and version is required ({@link #setRequireVersion(boolean)})
	 * @throws IncompatibleVersionsException if versions can't be compared
	 */
	protected AbstractResourceVersion getVersion(ResourceSpec[] specs) throws VersionException, IncompatibleVersionsException {
		if (_version != null) {
			return _version;
		}

		if (_resourceVersionProvider != null) {
			AbstractResourceVersion max = _minVersion;
			for (ResourceSpec spec : specs) {
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

	/**
	 * get the mount scope. Either use the manually set scope ({@link #setMountScope(Class)} or detect it. Default
	 * is to use the scope of all specs if it is common or use {@link ResourceMount}
	 * @param specs the specs to obtain the scope for
	 * @return the scope
	 */
	protected Class<?> getScope(ResourceSpec[] specs) {
		if (_mountScope != null) {
			return _mountScope;
		} else {
			Class<?> scope = null;
			for (ResourceSpec resourceSpec : specs) {
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

	/**
	 * create a new {@link ResourceReference}
	 * @param scope scope
	 * @param name name
	 * @param locale locale
	 * @param style style
	 * @param cacheDuration cache duration
	 * @param resourceSpecs resource specs
	 * @return a new {@link ResourceReference}
	 */
	protected ResourceReference newResourceReference(Class<?> scope, final String name, Locale locale, String style, int cacheDuration, ResourceSpec[] resourceSpecs) {
		ResourceReference ref;
		if (resourceSpecs.length > 1) {
			if (doCompress(name)) {
				if (doMinifyCss(name)) {
					ref = new CompressedMergedCssResourceReference(name, locale, style, resourceSpecs, cacheDuration);
				} else if (doMinifyJs(name)) {
					ref = new CompressedMergedJsResourceReference(name, locale, style, resourceSpecs, cacheDuration);
				} else {
					ref = new CompressedMergedResourceReference(name, locale, style, resourceSpecs, cacheDuration);
				}
			} else {
				ref = new MergedResourceReference(name, locale, style, resourceSpecs, cacheDuration);
			}
		} else if (resourceSpecs.length == 1) {
			ResourceSpec resourceSpec = resourceSpecs[0];
			if (resourceSpec.getRef() != null) {
				// reuse existing, e.g. for mountWicketResources(...)
				return resourceSpec.getRef();
			}
			
			if (doCompress(name)) {
				if (doMinifyCss(name)) {
					ref = new CachedCompressedCssResourceReference(scope, name, locale, style, cacheDuration);
				} else if (doMinifyJs(name)) {
					ref = new CachedCompressedJsResourceReference(scope, name, locale, style, cacheDuration);
				} else {
					ref = new CachedCompressedResourceReference(scope, name, locale, style, cacheDuration);
				}
			} else {
				ref = new CachedResourceReference(scope, name, locale, style, cacheDuration);
			}
		} else {
			throw new IllegalArgumentException("can't create ResourceReference without ResourceSpec");
		}
		return ref;
	}

	/**
	 * detect the locale to use. Either use a manually chosen one ({@link #setLocale(Locale)}) or detect it from
	 * the given resource specs. An {@link Exception} will be thrown if locales of added resources aren't compatible.
	 * (e.g. 'de' and 'en'). The resource will always use the most specific locale. For instance, if 5 resources
	 * are 'en' and one is 'en_US', the locale will be 'en_US'
	 * @param specs the {@link ResourceSpec}s to get the locale for
	 * @return the locale
	 */
	protected Locale getLocale(ResourceSpec[] specs) {
		if (_locale != null) {
			return _locale;
		}

		Locale locale = null;
		for (ResourceSpec spec : specs) {
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
	
	/**
	 * detect the style to use. Default implementation is to either use a manually chosen one ({@link #setStyle(String)}) or detect it from
	 * the given resource specs. An {@link Exception} will be thrown if styles of added resources aren't compatible.
	 * (e.g. 'foo' and 'bar', null and 'foo' are considered compatible). The resource will always use a style if at
	 * least one resource uses one. For instance, if 5 resources are don't have a style and one has 'foo', the style
	 * will be 'foo'
	 * @param specs the {@link ResourceSpec}s to get the style for
	 * @return the style
	 */
	protected String getStyle(ResourceSpec[] specs) {
		if (_style != null) {
			return _style;
		}
		
		String style = null;
		for (ResourceSpec spec : specs) {
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
	
	/**
	 * detect the cache duration to use. Default implementation is to either use a manually chosen one ({@link #setCacheDuration(int)})
	 * or detect it from the given resource specs. The resource will always use the lowest cache duration or {@link ResourceMount#DEFAULT_CACHE_DURATION}
	 * if it can't be detected
	 * @param specs the {@link ResourceSpec}s to get the cache duration for
	 * @return the cache duration in seconds
	 */
	protected int getCacheDuration(ResourceSpec[] specs) {
		if (_cacheDuration != null) {
			return _cacheDuration;
		}
		
		Integer cacheDuration = null;
		for (ResourceSpec spec : specs) {
			if (cacheDuration == null) {
				cacheDuration = spec.getCacheDuration();
			} else if (spec.getCacheDuration() != null && spec.getCacheDuration() < cacheDuration) {
				cacheDuration = spec.getCacheDuration();
			}
		}
		if (cacheDuration == null) {
			cacheDuration = DEFAULT_CACHE_DURATION;
		}
		return cacheDuration;
	}
	
	/**
	 * @return the resource specs
	 */
	protected ResourceSpec[] getResourceSpecs() {
		return _resourceSpecs.toArray(new ResourceSpec[_resourceSpecs.size()]);
	}

	/**
	 * @param file a file name
	 * @return whether this file should use gzip compression. default is to check the suffix of the file
	 * @see #setCompressed(boolean)
	 * @see #getCompressedSuffixes()
	 */
	protected boolean doCompress(final String file) {
		return _compressed == null ? _compressedSuffixes.contains(Strings.afterLast(file, '.')) : _compressed;
	}

	/**
	 * @param file a file name
	 * @return whether this file should be processed by a JS compressor. default is to minify files ending with '.js'
	 * @see #setMinifyJs(Boolean)
	 */
	protected boolean doMinifyJs(final String file) {
		return _minifyJs == null ? file.endsWith(".js") : _minifyJs;
	}

	/**
	 * @param file a file name
	 * @return whether this file should be processed by a CSS compressor. default is to minify files ending with '.css'
	 * @see #setMinifyJs(Boolean)
	 */
	protected boolean doMinifyCss(final String file) {
		return _minifyCss == null ? file.endsWith(".css") : _minifyCss;
	}

	/**
	 * should the added {@link ResourceSpec}s be merged to a single resource, or should they be mounted idividually?
	 * default is to merge files ending with {@link ResourceMount#DEFAULT_MERGE_SUFFIXES}
	 * @return
	 * @see #setMerged(boolean)
	 * @see #autodetectMerging()
	 * @see #getMergedSuffixes()
	 */
	protected boolean doMerge() {
		return _merge == null ? _resourceSpecs.size() > 1 && _mergedSuffixes.contains(Strings.afterLast(_path, '.')) : _merge;
	}
	
	/**
	 * clear all added {@link ResourceSpec}s
	 * @return this
	 */
	public ResourceMount clearSpecs() {
		_resourceSpecs.clear();
		return this;
	}

	/**
	 * @return the set of suffixes that will be compressed by default
	 * @see ResourceMount#DEFAULT_COMPRESS_SUFFIXES
	 */
	public Set<String> getCompressedSuffixes() {
		return _compressedSuffixes;
	}

	/**
	 * @return the set of suffixes that will be merged by default
	 * @see ResourceMount#DEFAULT_MERGE_SUFFIXES
	 */
	public Set<String> getMergedSuffixes() {
		return _mergedSuffixes;
	}

	/**
	 * a copy of the resource mount, with unfolded collections of compressed suffixes, merged suffices and {@link ResourceSpec}s
	 */
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

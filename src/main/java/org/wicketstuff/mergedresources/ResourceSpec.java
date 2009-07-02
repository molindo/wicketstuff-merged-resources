package org.wicketstuff.mergedresources;

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.util.string.Strings;

public class ResourceSpec {
	private final Class<?> _scope;
	private final String _file;
	private ResourceReference _ref;
	private Locale _locale;
	private String _style;
	private Integer _cacheDuration;
	
	public static ResourceSpec[] toResourceSpecs(Class<?>[] scopes, String[] files) {
		if (scopes.length != files.length) {
			throw new IllegalArgumentException("arrays must be of equal length: "
					+ Arrays.toString(scopes) + ", " + Arrays.toString(files));
		}
		ResourceSpec[] resourceSpecs = new ResourceSpec[scopes.length];
		for (int i = 0; i < scopes.length; i++) {
			resourceSpecs[i] = new ResourceSpec(scopes[i], files[i]);
		}
		return resourceSpecs;
	}
	
	public static Class<?>[] toScopes(ResourceSpec[] specs) {
		Class<?>[] scopes = new Class<?>[specs.length];
		for (int i = 0; i < specs.length; i++) {
			scopes[i] = specs[i].getScope();
		}
		return scopes;
	}
	
	public static String[] toFiles(ResourceSpec[] specs) {
		String[] files = new String[specs.length];
		for (int i = 0; i < specs.length; i++) {
			files[i] = specs[i].getFile();
		}
		return files;
	}
	
	public ResourceSpec(Class<?> scope, String file) {
		this(scope, file, null, null, null, null);
	}

	public ResourceSpec(Class<?> scope, String file, Locale locale) {
		this(scope, file, locale, null, null, null);
	}
	
	public ResourceSpec(Class<?> scope, String file, String style) {
		this(scope, file, null, style, null, null);
	}
	
	public ResourceSpec(Class<?> scope, String file, Locale locale, String style) {
		this(scope, file, locale, style, null, null);
	}
	
	public ResourceSpec(Class<?> scope, String file, Integer cacheDuration) {
		this(scope, file, null, null, cacheDuration, null);
	}

	public ResourceSpec(Class<?> scope, String file, Locale locale, Integer cacheDuration) {
		this(scope, file, locale, null, cacheDuration, null);
	}
	
	public ResourceSpec(Class<?> scope, String file, String style, Integer cacheDuration) {
		this(scope, file, null, style, cacheDuration, null);
	}
	
	public ResourceSpec(Class<?> scope, String file, Locale locale, String style, Integer cacheDuration) {
		this(scope, file, locale, style, cacheDuration, null);
	}
	
	public ResourceSpec(ResourceReference ref) {
		this(ref.getScope(), ref.getName(), ref.getLocale(), ref.getStyle(), null, ref);
		if (ref == null) {
			throw new NullPointerException("ref");
		}
	}

	private ResourceSpec(Class<?> scope, String file, Locale locale, String style, Integer cacheDuration, ResourceReference ref) {
		if (scope == null) {
			throw new NullPointerException("scope");
		}
		if (file == null) {
			throw new NullPointerException("file");
		}
		if (file.startsWith("/")) {
			throw new IllegalArgumentException("file must not start with '/', was '" + file + "'");
		}
		if (Strings.isEmpty(file)) {
			throw new IllegalArgumentException("file must not be empty");
		}
		
		_scope = scope;
		_file = file;
		_locale = locale;
		_style = style;
		_cacheDuration = cacheDuration;
		_ref = ref;
	}
	
	public Class<?> getScope() {
		return _scope;
	}

	/**
	 * @return file name, never starting with 
	 */
	public String getFile() {
		return _file;
	}

	/**
	 * @return might return null
	 */
	public Locale getLocale() {
		return _locale;
	}
	
	/**
	 * @return might return null
	 */
	public String getStyle() {
		return _style;
	}

	/**
	 * @return might return null or the max cache duration in seconds
	 */
	public Integer getCacheDuration() {
		return _cacheDuration;
	}

	/**
	 * @return {@link ResourceReference} that was used to construct this spec or null
	 */
	public ResourceReference getRef() {
		return _ref;
	}
}

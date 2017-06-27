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
package org.wicketstuff.mergedresources;

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.IClusterable;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.string.Strings;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ResourceSpec implements IClusterable {
	private static final long serialVersionUID = 1L;

	private final String _scopeName;
	@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "set from scopeName on demand")
	private transient Class<?> _scope;
	private final String _file;
	private final ResourceReference _ref;
	private final Locale _locale;
	private final String _style;
	private final Integer _cacheDuration;

	public static ResourceSpec[] toResourceSpecs(Class<?>[] scopes, String[] files) {
		if (scopes.length != files.length) {
			throw new IllegalArgumentException(
					"arrays must be of equal length: " + Arrays.toString(scopes) + ", " + Arrays.toString(files));
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
	}

	private ResourceSpec(Class<?> scope, String file, Locale locale, String style, Integer cacheDuration,
			ResourceReference ref) {
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
		_scopeName = _scope.getName();
		_file = file;
		_locale = locale;
		_style = style;
		_cacheDuration = cacheDuration;
		_ref = ref;
	}

	public Class<?> getScope() {
		if (_scope == null) {
			try {
				_scope = Class.forName(_scopeName);
			} catch (ClassNotFoundException e) {
				throw new WicketRuntimeException("failed to get scope class by name", e);
			}
		}
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
	 * @return {@link ResourceReference} that was used to construct this spec or
	 *         null
	 */
	public ResourceReference getRef() {
		return _ref;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _file.hashCode();
		result = prime * result + _scopeName.hashCode();
		result = prime * result + (_locale == null ? 0 : _locale.hashCode());
		result = prime * result + (_style == null ? 0 : _style.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ResourceSpec)) {
			return false;
		}
		ResourceSpec other = (ResourceSpec) obj;
		if (!_file.equals(other._file)) {
			return false;
		}
		if (!_scopeName.equals(other._scopeName)) {
			return false;
		}
		if (_locale == null) {
			if (other._locale != null) {
				return false;
			}
		} else if (!_locale.equals(other._locale)) {
			return false;
		}
		if (_style == null) {
			if (other._style != null) {
				return false;
			}
		} else if (!_style.equals(other._style)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceSpec [");
		if (_ref != null) {
			builder.append("ref=").append(_ref);
		} else {
			builder.append("file=").append(_file).append(", ");
			builder.append("scopeName=").append(_scopeName).append(", ");
			if (_locale != null) {
				builder.append("locale=").append(_locale).append(", ");
			}
			if (_style != null) {
				builder.append("style=").append(_style).append(", ");
			}
			if (_cacheDuration != null) {
				builder.append("cacheDuration=").append(_cacheDuration).append(", ");
			}

			builder.setLength(builder.length() - 2);
			builder.append("]");
		}
		return builder.toString();
	}

}

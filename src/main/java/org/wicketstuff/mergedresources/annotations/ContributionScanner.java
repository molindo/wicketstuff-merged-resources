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
package org.wicketstuff.mergedresources.annotations;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.config.MatchingResources;
import org.wicketstuff.mergedresources.ResourceSpec;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Gather page resources to merge, depends on {@link CssContribution} and {@link JsContribution} annotations.
 *
 * Helper to make using wicketstuff-merged-resources easier.
 */
public class ContributionScanner {

	private static final String DEFAULT_PATH_JS = "all.js";
	private static final String DEFAULT_PATH_CSS = "all.css";

	private final Map<String, SortedSet<WeightedResourceSpec>> _contributions;

	public ContributionScanner(final String packageName) {
		final MatchingResources resources = new MatchingResources(getPatternForPackage(packageName));

		_contributions = scan(resources);
	}

	private Map<String, SortedSet<WeightedResourceSpec>> scan(final MatchingResources resources) {
		final Map<String, SortedSet<WeightedResourceSpec>> contributions = new HashMap<>();

		for (final Class<?> cls : resources.getAnnotatedMatches(JsContribution.class)) {
			final JsContribution a = cls.getAnnotation(JsContribution.class);
			addJsContributions(cls, a, contributions);
		}

		for (final Class<?> cls : resources.getAnnotatedMatches(CssContribution.class)) {
			final CssContribution a = cls.getAnnotation(CssContribution.class);
			addCssContributions(cls, a, contributions);
		}

		for (final Class<?> cls : resources.getAnnotatedMatches(CssContributions.class)) {
			final CssContributions cssMulti = cls.getAnnotation(CssContributions.class);
			for (final CssContribution css : cssMulti.value()) {
				addCssContributions(cls, css, contributions);
			}
		}

		for (final Class<?> cls : resources.getAnnotatedMatches(ResourceContribution.class)) {
			final ResourceContribution resource = cls.getAnnotation(ResourceContribution.class);
			addResourceContributions(cls, resource, contributions);
		}

		for (final Map.Entry<String, SortedSet<WeightedResourceSpec>> e : contributions.entrySet()) {
			e.setValue(Collections.unmodifiableSortedSet(e.getValue()));
		}

		return Collections.unmodifiableMap(contributions);
	}

	private void addJsContributions(final Class<?> scope, final JsContribution js, final Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		for (String file : js.value()) {
			if (Strings.isEmpty(file)) {
				file = scope.getSimpleName() + ".js";
			}

			final String path = Strings.isEmpty(js.path()) ? DEFAULT_PATH_JS : js.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file, js.order()))) {
				throw new WicketRuntimeException("duplicate resource contribution: " + js + ", scope=" + scope);
			}
		}
	}

	private void addCssContributions(final Class<?> scope, final CssContribution css, final Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		for (String file : css.value()) {
			if (Strings.isEmpty(file)) {
				file = getDefaultCssFile(scope.getSimpleName(), css.media());
			}

			final String path = Strings.isEmpty(css.path()) ? getDefaultCssPath(css.media()) : css.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file, css.order()))) {
				throw new WicketRuntimeException("duplicate resource contribution: " + css + ", scope=" + scope);
			}
		}
	}

	static String getDefaultCssFile(final String simpleName, final String media) {
		if (!Strings.isEmpty(media) && !"all".equals(media)) {
			return simpleName + "-" + media + ".css";
		}
		return simpleName + ".css";
	}

	static String getDefaultCssPath(final String media) {
		if (!Strings.isEmpty(media)) {
			return media + ".css";
		}
		return DEFAULT_PATH_CSS;
	}

	private void addResourceContributions(final Class<?> scope, final ResourceContribution resource, final Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		for (final String file : resource.value()) {
			if (Strings.isEmpty(file)) {
				throw new WicketRuntimeException("empty file name not allowed for @ResourceContributions at class "
						+ scope.getName());
			}

			// don't merge resources by default
			final String path = Strings.isEmpty(resource.path()) ? file : resource.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file))) {
				throw new WicketRuntimeException("duplicate resource contribution: " + resource + ", scope=" + scope);
			}
		}
	}

	/**
	 * @return an unmodifiable map of contributions mapped by scope
	 */
	public Map<String, SortedSet<WeightedResourceSpec>> getContributions() {
		return _contributions;
	}

	/**
	 * Get the Spring search pattern given a package name or part of a package name
	 *
	 * @param packageName
	 *            a package name
	 * @return a Spring search pattern for the given package
	 */
	private String getPatternForPackage(String packageName) {
		if (packageName == null) {
			packageName = "";
		}
		packageName = packageName.replace('.', '/');
		if (!packageName.endsWith("/")) {
			packageName += '/';
		}

		return "classpath*:" + packageName + "**/*.class";
	}

	@SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "super type is sufficient, ignore weight")
	public static final class WeightedResourceSpec extends ResourceSpec {

		private static final long serialVersionUID = 1L;

		private final int _weight;

		public WeightedResourceSpec(final Class<?> scope, final String file, final int weight) {
			super(scope, file);
			_weight = weight;
		}

		public WeightedResourceSpec(final Class<?> scope, final String file) {
			this(scope, file, 0);
		}

		@Override
		public String toString() {
			return super.toString() + " (weight=" + _weight + ")";
		}
	}

	public enum WeightedResourceSpecComparator implements Comparator<WeightedResourceSpec> {
		INSTANCE;

		@Override
		public int compare(final WeightedResourceSpec o1, final WeightedResourceSpec o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			} else if (o2 == null) {
				return 1;
			}
			if (o1.equals(o2)) {
				return 0;
			}

			// from highest to lowest - avoid overflow
			int val = Integer.valueOf(o2._weight).compareTo(o1._weight);
			if (val != 0) {
				return val;
			}
			val = o1.getFile().compareTo(o2.getFile());
			if (val != 0) {
				return val;
			}
			return o1.getScope().getName().compareTo(o2.getScope().getName());
		}

	}
}

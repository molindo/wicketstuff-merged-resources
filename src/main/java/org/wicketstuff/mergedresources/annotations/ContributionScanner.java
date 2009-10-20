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

/**
 * Gather page resources to merge, depends on {@link CssContribution} and {@link JsContribution} annotations.
 * 
 * Helper to make using wicketstuff-merged-resources easier.
 */
public class ContributionScanner {

	private static final String DEFAULT_PATH_JS = "all.js";
	private static final String DEFAULT_PATH_CSS = "all.css";
	
	private final Map<String, SortedSet<WeightedResourceSpec>> _contributions;
	
	public ContributionScanner(String packageName) {
		MatchingResources resources = new MatchingResources(getPatternForPackage(packageName));
		
		_contributions = scan(resources);
	}
	
	private Map<String, SortedSet<WeightedResourceSpec>> scan(MatchingResources resources) {
		Map<String, SortedSet<WeightedResourceSpec>> contributions = new HashMap<String, SortedSet<WeightedResourceSpec>>();
		
		for (Class<?> cls : resources.getAnnotatedMatches(JsContribution.class)) {
			JsContribution a = cls.getAnnotation(JsContribution.class);
			addJsContributions(cls, a, contributions);
		}
	
		for (Class<?> cls : resources.getAnnotatedMatches(CssContribution.class)) {
			CssContribution a = cls.getAnnotation(CssContribution.class);
			addCssContributions(cls, a, contributions);
		}
	
		for (Class<?> cls : resources.getAnnotatedMatches(CssContributions.class)) {
			CssContributions cssMulti = cls.getAnnotation(CssContributions.class);
			for (CssContribution css : cssMulti.value()) {
				addCssContributions(cls, css, contributions);
			}
		}
		
		for (Class<?> cls : resources.getAnnotatedMatches(ResourceContribution.class)) {
			ResourceContribution resource = cls.getAnnotation(ResourceContribution.class);
			addResourceContributions(cls, resource, contributions);
		}
		
		for (Map.Entry<String, SortedSet<WeightedResourceSpec>> e : contributions.entrySet()) {
			e.setValue(Collections.unmodifiableSortedSet(e.getValue()));
		}
		
		return Collections.unmodifiableMap(contributions);
	}

	private void addJsContributions(Class<?> scope, JsContribution js, Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		for (String file : js.value()) {
			if (Strings.isEmpty(file)) {
				file = scope.getSimpleName() + ".js";
			}

			String path = Strings.isEmpty(js.path()) ? DEFAULT_PATH_JS : js.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<WeightedResourceSpec>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file, js.order()))) {
				throw new WicketRuntimeException("duplicate resource contribution: " + js + ", scope="+scope);
			}
		}
	}

	private void addCssContributions(Class<?> scope, CssContribution css, Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		for (String file : css.value()) {
			if (Strings.isEmpty(file)) {
				file = getDefaultCssFile(scope.getSimpleName(), css.media());
			}

			String path = Strings.isEmpty(css.path()) ? getDefaultCssPath(css.media()) : css.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<WeightedResourceSpec>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file, css.order()))) {
				throw new WicketRuntimeException("duplicate resource contribution: " + css + ", scope="+scope);
			}
		}
	}

	static String getDefaultCssFile(String simpleName, String media) {
		if (!Strings.isEmpty(media) && !"all".equals(media)) {
			return simpleName + "-" + media + ".css";
		}
		return simpleName + ".css";
	}

	static String getDefaultCssPath(String media) {
		if (!Strings.isEmpty(media)) {
			return media + ".css";
		}
		return DEFAULT_PATH_CSS;
	}

	private void addResourceContributions(Class<?> scope, ResourceContribution resource, Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		for (String file : resource.value()) {
			if (Strings.isEmpty(file)) {
				throw new WicketRuntimeException("empty file name not allowed for @ResourceContributions at class " + scope.getName());
			}

			// don't merge resources by default
			String path = Strings.isEmpty(resource.path()) ? file : resource.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<WeightedResourceSpec>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file))) {
				throw new WicketRuntimeException("duplicate resource contribution: " + resource + ", scope="+scope);
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
	 * Get the Spring search pattern given a package name or part of a package
	 * name
	 * 
	 * @param packageName
	 *            a package name
	 * @return a Spring search pattern for the given package
	 */
	private String getPatternForPackage(String packageName) {
		if (packageName == null)
			packageName = "";
		packageName = packageName.replace('.', '/');
		if (!packageName.endsWith("/")) {
			packageName += '/';
		}

		return "classpath*:" + packageName + "**/*.class";
	}

	public static final class WeightedResourceSpec extends ResourceSpec {

		private static final long serialVersionUID = 1L;
		
		private int _weight;

		public WeightedResourceSpec(Class<?> scope, String file, int weight) {
			super(scope, file);
			_weight = weight;
		}

		public WeightedResourceSpec(Class<?> scope, String file) {
			this(scope, file, 0);
		}
		
		public String toString() {
			return super.toString() + " (weight="+_weight+")";
		}
	}
	
	public enum WeightedResourceSpecComparator implements Comparator<WeightedResourceSpec> {
		INSTANCE;

		public int compare(WeightedResourceSpec o1, WeightedResourceSpec o2) {
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
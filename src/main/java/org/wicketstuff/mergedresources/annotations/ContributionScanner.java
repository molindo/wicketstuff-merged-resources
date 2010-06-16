package org.wicketstuff.mergedresources.annotations;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.Component;
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
	private final Set<String> _enabledClassNames;
	
	public ContributionScanner(String packageName) {
		MatchingResources resources = new MatchingResources(getPatternForPackage(packageName));
		_enabledClassNames = new HashSet<String>();
		_contributions = scan(resources);
	}
	
	private Map<String, SortedSet<WeightedResourceSpec>> scan(MatchingResources resources) {
		Map<String, SortedSet<WeightedResourceSpec>> contributions = new HashMap<String, SortedSet<WeightedResourceSpec>>();
		
		for (Class<?> cls : resources.getAnnotatedMatches(JsContribution.class, true)) {
			JsContribution a = cls.getAnnotation(JsContribution.class);
			_enabledClassNames.add(cls.getName());
			if (a != null) {
				addJsContributions(cls, a, contributions);
			}
		}
	
		for (Class<?> cls : resources.getAnnotatedMatches(CssContribution.class, true)) {
			CssContribution a = cls.getAnnotation(CssContribution.class);
			_enabledClassNames.add(cls.getName());
			if (a != null) {
				addCssContributions(cls, a, contributions);
			}
		}
	
		for (Class<?> cls : resources.getAnnotatedMatches(CssContributions.class, true)) {
			CssContributions cssMulti = cls.getAnnotation(CssContributions.class);
			_enabledClassNames.add(cls.getName());
			if (cssMulti != null) {
				for (CssContribution css : cssMulti.value()) {
					addCssContributions(cls, css, contributions);
				}
			}
		}
		
		for (Class<?> cls : resources.getAnnotatedMatches(ResourceContribution.class, true)) {
			ResourceContribution resource = cls.getAnnotation(ResourceContribution.class);
			_enabledClassNames.add(cls.getName());
			if (resource != null) {
				addResourceContributions(cls, resource, contributions);
			}
		}
		
		for (Map.Entry<String, SortedSet<WeightedResourceSpec>> e : contributions.entrySet()) {
			e.setValue(Collections.unmodifiableSortedSet(e.getValue()));
		}
		
		return Collections.unmodifiableMap(contributions);
	}

	private void addJsContributions(Class<?> scope, JsContribution js, Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		String[] values = js.value();
		for (int i = 0; i < values.length; i++) {
			String file = values[i];
			if (Strings.isEmpty(file)) {
				file = scope.getSimpleName() + ".js";
			}

			String path = Strings.isEmpty(js.path()) ? DEFAULT_PATH_JS : js.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<WeightedResourceSpec>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file, js.order(), values.length - i))) {
				throw new WicketRuntimeException("duplicate resource contribution: " + js + ", scope=" + scope);
			}
		}
	}

	private void addCssContributions(Class<?> scope, CssContribution css, Map<String, SortedSet<WeightedResourceSpec>> contributions) {
		String[] values = css.value();
		for (int i = 0; i < values.length; i++) {
			String file = values[i];
			if (Strings.isEmpty(file)) {
				file = getDefaultCssFile(scope.getSimpleName(), css.media());
			}

			String path = Strings.isEmpty(css.path()) ? getDefaultCssPath(css.media()) : css.path();
			SortedSet<WeightedResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new TreeSet<WeightedResourceSpec>(WeightedResourceSpecComparator.INSTANCE);
				contributions.put(path, specs);
			}
			if (!specs.add(new WeightedResourceSpec(scope, file, css.order(), values.length - i))) {
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

	private List<String> mergeAnnontations(Class<?> scope, List<JsContribution> contribs) {
		Collections.sort(contribs, new Comparator<JsContribution>() {
			public int compare(JsContribution o1, JsContribution o2) {
				return o1.order() - o2.order();
			}
		});
		List<String> ret = new ArrayList<String>();
		for (JsContribution jsContribution : contribs) {
			String[] values = jsContribution.value();
			for (String val : values) {
				ret.add(val);
			}
		}
		return ret;
	}

	private <T extends Annotation> List<T> getAllMatchingAnnotations(Class<?> clz, Class<T> annotation) {
		return getAllMatchingAnnotations(new ArrayList<T>(1), clz, annotation);
	}

	private <T extends Annotation> List<T> getAllMatchingAnnotations(List<T> matchedList, Class<?> clz, Class<T> annotation) {
		if (clz == null) {
			return matchedList;
		}

		if (clz.isAnnotationPresent(annotation)) {
			matchedList.add(clz.getAnnotation(annotation));
		}
		return getAllMatchingAnnotations(matchedList, clz.getSuperclass(), annotation);
	}

	public static final class WeightedResourceSpec extends ResourceSpec {

		private static final long serialVersionUID = 1L;
		
		private int _weight;

		private int _minorWeight;

		public WeightedResourceSpec(Class<?> scope, String file, int weight, int minorWeight) {
			super(scope, file);
			_weight = weight;
			_minorWeight = minorWeight;
		}

		public WeightedResourceSpec(Class<?> scope, String file) {
			this(scope, file, 0, 0);
		}
		
		@Override
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
			val = Integer.valueOf(o2._minorWeight).compareTo(o1._minorWeight);
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

	public boolean hasContribution(Component component) {
		return _enabledClassNames.contains(component.getClass().getName());
	}
}
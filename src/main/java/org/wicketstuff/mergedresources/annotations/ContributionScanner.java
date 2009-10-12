package org.wicketstuff.mergedresources.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	private final Map<String, Set<ResourceSpec>> _contributions;
	
	public ContributionScanner(String packageName) {
		MatchingResources resources = new MatchingResources(getPatternForPackage(packageName));
		
		_contributions = scan(resources);
	}
	
	private Map<String, Set<ResourceSpec>> scan(MatchingResources resources) {
		Map<String, Set<ResourceSpec>> contributions = new HashMap<String, Set<ResourceSpec>>();
		
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
		
		for (Map.Entry<String, Set<ResourceSpec>> e : contributions.entrySet()) {
			e.setValue(Collections.unmodifiableSet(e.getValue()));
		}
		
		return Collections.unmodifiableMap(contributions);
	}

	private void addJsContributions(Class<?> scope, JsContribution js, Map<String, Set<ResourceSpec>> contributions) {
		for (String file : js.value()) {
			if (Strings.isEmpty(file)) {
				file = scope.getSimpleName() + ".js";
			}

			String path = Strings.isEmpty(js.path()) ? DEFAULT_PATH_JS : js.path();
			Set<ResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new HashSet<ResourceSpec>();
				contributions.put(path, specs);
			}
			specs.add(new ResourceSpec(scope, file));
		}
	}

	private void addCssContributions(Class<?> scope, CssContribution css, Map<String, Set<ResourceSpec>> contributions) {
		for (String file : css.value()) {
			if (Strings.isEmpty(file)) {
				file = getDefaultCssFile(scope.getSimpleName(), css.media());
			}

			String path = Strings.isEmpty(css.path()) ? getDefaultCssPath(css.media()) : css.path();
			Set<ResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new HashSet<ResourceSpec>();
				contributions.put(path, specs);
			}
			specs.add(new ResourceSpec(scope, file));
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

	private void addResourceContributions(Class<?> scope, ResourceContribution resource, Map<String, Set<ResourceSpec>> contributions) {
		for (String file : resource.value()) {
			if (Strings.isEmpty(file)) {
				throw new WicketRuntimeException("empty file name not allowed for @ResourceContributions at class " + scope.getName());
			}

			// don't merge resources by default
			String path = Strings.isEmpty(resource.path()) ? file : resource.path();
			Set<ResourceSpec> specs = contributions.get(path);
			if (specs == null) {
				specs = new HashSet<ResourceSpec>();
				contributions.put(path, specs);
			}
			specs.add(new ResourceSpec(scope, file));
		}
	}
	
	/**
	 * @return an unmodifiable map of contributions mapped by scope
	 */
	public Map<String, Set<ResourceSpec>> getContributions() {
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

}
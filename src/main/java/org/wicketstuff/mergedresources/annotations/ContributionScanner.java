package org.wicketstuff.mergedresources.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.util.string.Strings;
import org.wicketstuff.config.MatchingResources;
import org.wicketstuff.mergedresources.ResourceSpec;

/**
 * Gather page resources to merge, depends on {@link CssContribution} and {@link JsContribution} annotations.
 * 
 * Helper to make using wicketstuff-merged-resources easier.
 */
public class ContributionScanner {

	private final Map<String, Set<ResourceSpec>> _contributions;
	
	public ContributionScanner(String packageName) {
		MatchingResources resources = new MatchingResources(getPatternForPackage(packageName));
		
		_contributions = scan(resources);
	}
	
	private Map<String, Set<ResourceSpec>> scan(MatchingResources resources) {
		Map<String, Set<ResourceSpec>> contributions = new HashMap<String, Set<ResourceSpec>>();
		
		{
			// js
			for (Class<?> cls : resources.getAnnotatedMatches(JsContribution.class)) {
				JsContribution a = cls.getAnnotation(JsContribution.class);
				
				for (String file : a.value()) {
					if (Strings.isEmpty(file)) {
						file = cls.getSimpleName() + ".js";
					}

					String path = Strings.isEmpty(a.path()) ? file : a.path();
					Set<ResourceSpec> specs = contributions.get(path);
					if (specs == null) {
						specs = new HashSet<ResourceSpec>();
						contributions.put(path, specs);
					}
					specs.add(new ResourceSpec(cls, file));
				}
			}
		}
		
		{
			for (Class<?> cls : resources.getAnnotatedMatches(CssContribution.class)) {
				CssContribution a = cls.getAnnotation(CssContribution.class);
							
				for (String file : a.value()) {
					if (Strings.isEmpty(file)) {
						file = cls.getSimpleName() + ".css";
					}

					String path = Strings.isEmpty(a.path()) ? file : a.path();
					Set<ResourceSpec> specs = contributions.get(path);
					if (specs == null) {
						specs = new HashSet<ResourceSpec>();
						contributions.put(path, specs);
					}
					specs.add(new ResourceSpec(cls, file));
				}
			}
		}
		
		for (Map.Entry<String, Set<ResourceSpec>> e : contributions.entrySet()) {
			e.setValue(Collections.unmodifiableSet(e.getValue()));
		}
		
		return Collections.unmodifiableMap(contributions);
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
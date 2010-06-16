package org.wicketstuff.mergedresources.annotations;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.util.string.Strings;

public class HeaderContribution extends AbstractHeaderContributor {
	
	private static final long serialVersionUID = 1L;
	
	private List<IHeaderContributor> _headerContributors = new ArrayList<IHeaderContributor>(5);
	
	/**
	 * Reads contributions from {@link JsContribution} and {@link CssContribution} annotations.
	 */
	@SuppressWarnings("unchecked")
	public HeaderContribution(Class<? extends Component> scope) {
		List<Class<?>> jsClasses = getClassesWithAnnotation(scope, JsContribution.class);
		for (Class<?> clz : jsClasses) {
			addJsContributions((Class<? extends Component>) clz, clz.getAnnotation(JsContribution.class));
		}
		
		List<Class<?>> cssClasses = getClassesWithAnnotation(scope, CssContribution.class);
		for (Class<?> clz : cssClasses) {
			addCssContributions((Class<? extends Component>) clz, clz.getAnnotation(CssContribution.class));
		}

		List<Class<?>> cssMultiClasses = getClassesWithAnnotation(scope, CssContributions.class);
		for (Class<?> clz : cssMultiClasses) {
			CssContributions cssMulti = clz.getAnnotation(CssContributions.class);
			if (cssMulti != null) {
				for (CssContribution css : cssMulti.value()) {
					addCssContributions(scope, css);
				}
			}
		}
	}

	private void addCssContributions(Class<? extends Component> scope,
			CssContribution css) {
		if (css != null) {
			String[] stylesheets = replaceDefault(css.value(), ContributionScanner.getDefaultCssFile(scope.getSimpleName(), css.media()));
			String stylesheetsMedia = Strings.isEmpty(css.media()) ? null : css.media();
			for (String stylesheet : stylesheets) {
				if (stylesheetsMedia == null) {
					_headerContributors.add(CSSPackageResource.getHeaderContribution(scope, stylesheet));
				} else {
					_headerContributors.add(CSSPackageResource.getHeaderContribution(scope, stylesheet, stylesheetsMedia));
				}
			}
		}
	}

	private void addJsContributions(Class<? extends Component> scope, JsContribution js) {
		if (js != null){
			String[] scripts = replaceDefault(js.value(), scope.getSimpleName() + ".js");
			for(String script : scripts) {
				_headerContributors.add(JavascriptPackageResource.getHeaderContribution(scope, script));
			}
		}
	}

	private String[] replaceDefault(String[] files, String defaulFile) {
		for (int i = 0; i < files.length; i++) {
			if (Strings.isEmpty(files[i])) {
				files[i] = defaulFile;
			}
		}
		return files;
	}

	@Override
	public IHeaderContributor[] getHeaderContributors() {
		return _headerContributors.toArray(new IHeaderContributor[_headerContributors.size()]);
	}

	private List<Class<?>> getClassesWithAnnotation(Class<?> clz, Class<? extends Annotation> annotation) {
		return getClassesWithAnnotation(new ArrayList<Class<?>>(1), clz, annotation);
	}

	private List<Class<?>> getClassesWithAnnotation(List<Class<?>> matchedList, Class<?> clz, Class<? extends Annotation> annotation) {
		if (clz == null) {
			return matchedList;
		}

		if (clz.isAnnotationPresent(annotation)) {
			matchedList.add(clz);
		}
		return getClassesWithAnnotation(matchedList, clz.getSuperclass(), annotation);
	}
	
}
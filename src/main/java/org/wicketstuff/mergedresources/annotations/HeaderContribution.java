package org.wicketstuff.mergedresources.annotations;

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
	public HeaderContribution(Class<? extends Component> scope) {
		addJsContributions(scope, scope.getAnnotation(JsContribution.class));
		addCssContributions(scope, scope.getAnnotation(CssContribution.class));
		
		CssContributions cssMulti = scope.getAnnotation(CssContributions.class);
		if (cssMulti != null) {
			for (CssContribution css : cssMulti.value()) {
				addCssContributions(scope, css);
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
	
}
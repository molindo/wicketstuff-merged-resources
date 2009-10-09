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
	
	private final String[] _scripts;
	private final String[] _stylesheets;
	private final String _stylesheetsMedia;

	private List<IHeaderContributor> _headerContributors;
	
	/**
	 * Reads contributions from {@link JsContribution} and {@link CssContribution} annotations.
	 */
	public HeaderContribution(Class<? extends Component> scope) {
		JsContribution js = scope.getAnnotation(JsContribution.class);
		if (js == null) {
			_scripts = new String[0];
		} else {
			_scripts = replaceDefault(js.value(), scope.getSimpleName() + ".js");
		}
		
		CssContribution css = scope.getAnnotation(CssContribution.class);
		if (css == null) {
			_stylesheets = new String[0];
			_stylesheetsMedia = null;
		} else {
			_stylesheets = replaceDefault(css.value(), scope.getSimpleName() + ".css");
			_stylesheetsMedia = Strings.isEmpty(css.media()) ? null : css.media();
		}
		
		_headerContributors = new ArrayList<IHeaderContributor>();
		for (String stylesheet : _stylesheets) {
			if (_stylesheetsMedia == null) {
				_headerContributors.add(CSSPackageResource.getHeaderContribution(scope, stylesheet));
			} else {
				_headerContributors.add(CSSPackageResource.getHeaderContribution(scope, stylesheet, _stylesheetsMedia));
			}
		}
		
		for(String script : _scripts) {
			_headerContributors.add(JavascriptPackageResource.getHeaderContribution(scope, script));
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
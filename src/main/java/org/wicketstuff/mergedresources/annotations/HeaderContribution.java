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

	private final List<IHeaderContributor> _headerContributors = new ArrayList<>(5);

	/**
	 * Reads contributions from {@link JsContribution} and {@link CssContribution} annotations.
	 */
	public HeaderContribution(final Class<? extends Component> scope) {
		addJsContributions(scope, scope.getAnnotation(JsContribution.class));
		addCssContributions(scope, scope.getAnnotation(CssContribution.class));

		final CssContributions cssMulti = scope.getAnnotation(CssContributions.class);
		if (cssMulti != null) {
			for (final CssContribution css : cssMulti.value()) {
				addCssContributions(scope, css);
			}
		}
	}

	private void addCssContributions(final Class<? extends Component> scope, final CssContribution css) {
		if (css != null) {
			final String[] stylesheets = replaceDefault(css.value(), ContributionScanner
					.getDefaultCssFile(scope.getSimpleName(), css.media()));
			final String stylesheetsMedia = Strings.isEmpty(css.media()) ? null : css.media();
			for (final String stylesheet : stylesheets) {
				if (stylesheetsMedia == null) {
					_headerContributors.add(CSSPackageResource.getHeaderContribution(scope, stylesheet));
				} else {
					_headerContributors
							.add(CSSPackageResource.getHeaderContribution(scope, stylesheet, stylesheetsMedia));
				}
			}
		}
	}

	private void addJsContributions(final Class<? extends Component> scope, final JsContribution js) {
		if (js != null) {
			final String[] scripts = replaceDefault(js.value(), scope.getSimpleName() + ".js");
			for (final String script : scripts) {
				_headerContributors.add(JavascriptPackageResource.getHeaderContribution(scope, script));
			}
		}
	}

	private String[] replaceDefault(final String[] files, final String defaulFile) {
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

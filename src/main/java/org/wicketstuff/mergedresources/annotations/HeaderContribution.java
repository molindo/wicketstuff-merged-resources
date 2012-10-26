/**
 * Copyright 2010 Molindo GmbH
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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.string.Strings;

import java.util.ArrayList;
import java.util.List;

public class HeaderContribution extends Behavior {

	private static final long serialVersionUID = 1L;

	private List<HeaderItem> _headerContributors = new ArrayList<HeaderItem>(5);

	/** Reads contributions from {@link JsContribution} and {@link CssContribution} annotations. */
	public HeaderContribution(Class<? extends Component> scope) {
		addJsContributions(scope, scope.getAnnotation(JsContribution.class));
		addCssContributions(scope, scope.getAnnotation(CssContribution.class));
		final CssContributions cssMulti = scope.getAnnotation(CssContributions.class);
		if (cssMulti != null) {
			for (CssContribution css : cssMulti.value()) {
				addCssContributions(scope, css);
			}
		}
	}

	private void addCssContributions(Class<? extends Component> scope, CssContribution css) {
		if (css != null) {
			final String[] stylesheets = replaceDefault(css.value(), ContributionScanner.getDefaultCssFile(scope.getSimpleName(), css.media()));
			final String stylesheetsMedia = Strings.isEmpty(css.media()) ? null : css.media();
			for (String stylesheet : stylesheets) {
				if (stylesheetsMedia == null) {
					_headerContributors.add(CssHeaderItem.forReference(new CssResourceReference(scope, stylesheet)));
				} else {
					_headerContributors.add(CssHeaderItem.forReference(new CssResourceReference(scope, stylesheet), stylesheetsMedia));
				}
			}
		}
	}

	private void addJsContributions(Class<? extends Component> scope, JsContribution js) {
		if (js != null) {
			final String[] scripts = replaceDefault(js.value(), scope.getSimpleName() + ".js");
			for (String script : scripts) {
				_headerContributors.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(scope, script)));
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
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		for (HeaderItem headerItem : _headerContributors) {
			response.render(headerItem);
		}
	}

}
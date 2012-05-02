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

package org.wicketstuff.mergedresources.util;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.Strings;

import java.util.ArrayList;
import java.util.List;

public class MergedHeaderContributor extends Behavior {

	private static final long serialVersionUID = 1L;
	private Behavior _contributor;
	private ArrayList<ResourceReference> _refs;
	private String _cssMediaType;

	public MergedHeaderContributor(final List<ResourceReference> refs) {
		this(refs, null);
	}

	public MergedHeaderContributor(List<ResourceReference> refs, String cssMediaType) {
		_refs = new ArrayList<ResourceReference>(refs);
		_cssMediaType = cssMediaType;
		_contributor = new Behavior() {

			private static final long serialVersionUID = 1L;

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				for (final ResourceReference ref : _refs) {
					final String name = ref.getName();
					if (name != null) {
						if (name.endsWith(".js")) {
							response.renderJavaScriptReference(ref);
						} else if (name.endsWith(".css")) {
							if (Strings.isEmpty(_cssMediaType)) {
								response.renderCSSReference(ref);
							} else {
								response.renderCSSReference(ref, _cssMediaType);
							}
						}
					}
				}
			}
		};
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		this._contributor.renderHead(component, response);
	}

}

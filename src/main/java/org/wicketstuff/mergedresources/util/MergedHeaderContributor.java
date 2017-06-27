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
package org.wicketstuff.mergedresources.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.util.string.Strings;

public class MergedHeaderContributor extends AbstractHeaderContributor {
	private static final long serialVersionUID = 1L;
	public IHeaderContributor _contributor;
	private final ArrayList<ResourceReference> _refs;
	private final String _cssMediaType;

	public MergedHeaderContributor(final List<ResourceReference> refs) {
		this(refs, null);
	}

	public MergedHeaderContributor(final List<ResourceReference> refs, final String cssMediaType) {
		_refs = new ArrayList<>(refs);
		_cssMediaType = cssMediaType;
		_contributor = new IHeaderContributor() {

			private static final long serialVersionUID = 1L;

			@Override
			public void renderHead(final IHeaderResponse response) {
				for (final ResourceReference ref : _refs) {
					final String name = ref.getName();
					if (name != null) {
						if (name.endsWith(".js")) {
							response.renderJavascriptReference(ref);
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
	public IHeaderContributor[] getHeaderContributors() {
		return new IHeaderContributor[] { _contributor };
	}
}

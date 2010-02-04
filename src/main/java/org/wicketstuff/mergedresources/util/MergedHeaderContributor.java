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
	private ArrayList<ResourceReference> _refs;
	private String _cssMediaType;

	public MergedHeaderContributor(final List<ResourceReference> refs) {
		this(refs, null);
	}

	public MergedHeaderContributor(List<ResourceReference> refs, String cssMediaType) {
		_refs = new ArrayList<ResourceReference>(refs);
		_cssMediaType = cssMediaType;
		_contributor = new IHeaderContributor() {

			private static final long serialVersionUID = 1L;

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

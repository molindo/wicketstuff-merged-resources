package org.wicketstuff.mergedresources.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

public class MergedHeaderContributor extends AbstractHeaderContributor {
	private static final long serialVersionUID = 1L;
	public List<ResourceReference> _refs;
	public IHeaderContributor _contributor = new IHeaderContributor() {

		private static final long serialVersionUID = 1L;

		public void renderHead(final IHeaderResponse response) {
			for (final ResourceReference ref : _refs) {
				final String name = ref.getName();
				if (name != null) {
					if (name.endsWith(".js")) {
						response.renderJavascriptReference(ref);
					} else if (name.endsWith(".css")) {
						response.renderCSSReference(ref);
					}
				}
			}
		}
	};

	public MergedHeaderContributor(final List<ResourceReference> refs) {
		_refs = new ArrayList<ResourceReference>(refs);
	}

	@Override
	public IHeaderContributor[] getHeaderContributors() {
		return new IHeaderContributor[] { _contributor };
	}
}

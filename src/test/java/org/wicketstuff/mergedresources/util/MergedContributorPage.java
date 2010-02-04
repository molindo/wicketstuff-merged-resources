package org.wicketstuff.mergedresources.util;

import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.mergedresources.NewInterfaceTestApplication;

public class MergedContributorPage extends WebPage {

	public MergedContributorPage() {
		add(((NewInterfaceTestApplication)getApplication()).getJsContributor());
		add(((NewInterfaceTestApplication)getApplication()).getCssContributor());
		add(((NewInterfaceTestApplication)getApplication()).getCssPrintContributor());
	}
}

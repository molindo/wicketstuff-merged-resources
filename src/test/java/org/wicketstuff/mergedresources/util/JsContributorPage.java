package org.wicketstuff.mergedresources.util;

import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.mergedresources.NewInterfaceTestApplication;

public class JsContributorPage extends WebPage {

	public JsContributorPage() {
		add(((NewInterfaceTestApplication)getApplication()).getJsContributor());
	}
}

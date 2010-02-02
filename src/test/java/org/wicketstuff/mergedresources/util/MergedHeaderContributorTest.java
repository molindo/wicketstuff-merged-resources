package org.wicketstuff.mergedresources.util;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.wicketstuff.mergedresources.NewInterfaceTestApplication;
import org.wicketstuff.mergedresources.ResourceMount;

public class MergedHeaderContributorTest {
	@Test
	public void renderJsContributorPage() throws Exception {
		WicketTester tester = new WicketTester(
				new NewInterfaceTestApplication() {

					protected void initMount(ResourceMount mount) {
						mount.setMerged(false);
					}
					
				});
		
        tester.startPage(JsContributorPage.class);
        tester.assertRenderedPage(JsContributorPage.class);
		tester.assertResultPage(JsContributorPage.class,
				"JsContributorPage-expected.html");
	}
}

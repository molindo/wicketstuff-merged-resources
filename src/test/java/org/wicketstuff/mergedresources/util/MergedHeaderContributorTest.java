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

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.wicketstuff.mergedresources.NewInterfaceTestApplication;
import org.wicketstuff.mergedresources.ResourceMount;

public class MergedHeaderContributorTest {
	@Test
	public void renderJsContributorPage() throws Exception {
		final WicketTester tester = new WicketTester(new NewInterfaceTestApplication() {

			@Override
			protected void initMount(final ResourceMount mount) {
				mount.setMerged(false);
			}

		});

		tester.startPage(MergedContributorPage.class);
		tester.assertRenderedPage(MergedContributorPage.class);
		tester.assertResultPage(MergedContributorPage.class, "MergedContributorPage-expected.html");
	}
}

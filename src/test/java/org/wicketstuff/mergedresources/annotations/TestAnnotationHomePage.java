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

import junit.framework.TestCase;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.wicketstuff.mergedresources.annotations.components.PanelOne;
import org.wicketstuff.mergedresources.util.WicketResourceTester;

/**
 * Simple test using the WicketTester
 */
public class TestAnnotationHomePage extends TestCase {
	private WicketResourceTester tester;

	public void setUp() {
		tester = new WicketResourceTester(new AnnotationTestApplication());
	}

	public void testRenderMyPage() {
		assertEquals("test must run in deployment mode", tester.getApplication().getConfigurationType(), RuntimeConfigurationType.DEPLOYMENT);

		// start and render the test page
		tester.startPage(tester.getApplication().getHomePage());

		// assert rendered page class
		tester.assertRenderedPage(tester.getApplication().getHomePage());

		System.out.println(tester.getLastResponseAsString());
		assertTrue(tester.ifContains("resources/").wasFailed());
		assertFalse(tester.ifContains("files/all-[0-9]+\\.css").wasFailed());
		assertFalse(tester.ifContains("files/all-[0-9]+\\.js").wasFailed());
		assertFalse(tester.ifContains("files/print-[0-9]+\\.css").wasFailed());
		assertFalse(tester.ifContains("files/forms-[0-9]+\\.js").wasFailed());
		assertFalse(tester.ifContains("files/forms-[0-9]+\\.css").wasFailed());
		// does anybody know how to check resources?

		assertTrue(tester.urlFor(WicketAjaxReference.INSTANCE).matches("script/wicket-ajax.*\\.js"));
		assertTrue(tester.urlFor(WicketEventReference.INSTANCE).matches("script/wicket-event.*\\.js"));
		assertTrue(tester.urlFor(new PackageResourceReference(PanelOne.class, "PanelOne.css")).matches("files/all-[0-9]+\\.css"));
		assertTrue(tester.urlFor(new PackageResourceReference(PanelOne.class, "PanelOne-print.css")).matches("files/print-[0-9]+\\.css"));
		assertTrue(tester.urlFor(new PackageResourceReference(PanelOne.class, "functions.js")).matches("files/all-[0-9]+\\.js"));
		assertTrue(tester.urlFor(new PackageResourceReference(PanelOne.class, "accept.png")).matches("img/accept-[0-9]+\\.png"));
	}
}

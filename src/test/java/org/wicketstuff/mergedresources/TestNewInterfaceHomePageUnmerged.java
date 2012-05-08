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

package org.wicketstuff.mergedresources;

import junit.framework.TestCase;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Simple test using the WicketTester
 */
public class TestNewInterfaceHomePageUnmerged extends TestCase {
	private WicketTester tester;

	public void setUp() {
		tester = new WicketTester(new NewInterfaceTestApplication() {
			@Override
			public RuntimeConfigurationType getConfigurationType() {
				return RuntimeConfigurationType.DEVELOPMENT;
			}

			@Override
			protected boolean merge() {
				return false;
			}

			@Override
			protected boolean strip() {
				return true;
			}
		});
	}

	public void testRenderMyPage() {
		// start and render the test page
		tester.startPage(HomePage.class);

		// assert rendered page class
		tester.assertRenderedPage(HomePage.class);

		assertTrue(tester.ifContains("style/all-[0-9]+\\.css").wasFailed());
		assertTrue(tester.ifContains("script/all-[0-9]+\\.js").wasFailed());
		assertFalse(tester.ifContains("wicket/resource/").wasFailed());

		// does anybody know how to check resources?
	}
}

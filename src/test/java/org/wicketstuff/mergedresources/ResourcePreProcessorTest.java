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
package org.wicketstuff.mergedresources;

import org.apache.wicket.util.tester.WicketTester;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.PanelOne;
import org.wicketstuff.mergedresources.preprocess.StringResourcePreProcessor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import junit.framework.TestCase;

/**
 * Simple test using the WicketTester
 */
@SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "no serialization in test case")
public class ResourcePreProcessorTest extends TestCase {
	private int _preProcessInvocations = 0;
	private int _preProcessInvocationsMerged = 0;

	public void testRenderMyPage() {
		new WicketTester(new AbstractTestApplication() {

			@Override
			protected void mountResources() {
				ResourceMount m = new ResourceMount();
				m.setPreProcessor(new StringResourcePreProcessor() {

					private static final long serialVersionUID = 1L;

					@Override
					protected String preProcess(ResourceSpec resourceSpec, String string) {
						if (resourceSpec != null) {
							_preProcessInvocations++;
						} else {
							_preProcessInvocationsMerged++;
						}
						// System.out.println("process " + string);
						return string;
					}
				});
				m.setPath("foo.js").addResourceSpecsMatchingSuffix(PanelOne.class, MyForm.class).mount(this);

				m.setPath("bar.js").addResourceSpecsMatchingSuffix(ComponentB.class).mount(this);
			}
		});
		assertEquals(5, _preProcessInvocations);
		assertEquals(2, _preProcessInvocationsMerged);
	}
}

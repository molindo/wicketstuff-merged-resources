/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.mergedresources;

import junit.framework.TestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.PanelOne;
import org.wicketstuff.mergedresources.preprocess.StringResourcePreProcessor;


/**
 * Simple test using the WicketTester
 */
public class ResourcePreProcessorTest extends TestCase
{
	private int _preProcessInvocations = 0;
	
	public void testRenderMyPage()
	{
		new WicketTester(new AbstractTestApplication() {
			
			@Override
			protected void mountResources() {
				ResourceMount m = new ResourceMount();
				m.setPreProcessor(new StringResourcePreProcessor() {
					
					@Override
					protected String preProcess(String string) {
						_preProcessInvocations++;
						//System.out.println("process " + string);
						return string;
					}
				});
				m.setPath("foo.js")
					.addResourceSpecsMatchingSuffix(PanelOne.class, MyForm.class)
					.mount(this);
				
				m.setPath("bar.js")
					.addResourceSpecsMatchingSuffix(ComponentB.class)
					.mount(this);
			}
		});
		assertEquals(2, _preProcessInvocations);
	}
}

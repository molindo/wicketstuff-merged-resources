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
package org.wicketstuff.mergedresources.annotations;

import junit.framework.TestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.wicketstuff.mergedresources.ResourceMount;
import org.wicketstuff.mergedresources.preprocess.StringResourcePreProcessor;


/**
 * Simple test using the WicketTester
 */
public class ResourceMergeOrderTest extends TestCase
{
	private boolean _testedPanelOneCss = false;
	
	public void testRenderMyPage()
	{
		new WicketTester(new AnnotationTestApplication() {
			
			protected boolean strip() {
				return false;
			}
			
			protected ResourceMount newResourceMount() {
				ResourceMount mount = super.newResourceMount();
				mount.setPreProcessor(new StringResourcePreProcessor() {
					
					@Override
					protected String preProcess(String string) {
						if (string.contains(".panelOne")) {
							String s = string.trim();
							s = s.substring(s.lastIndexOf('\n'));
							assertTrue(s.contains("LAST LINE OF MERGED RESOURCE"));
							_testedPanelOneCss = true;
						}
						return string;
					}
				});
				return mount;
			}
		});
		assertTrue("PanelOne.css not processed", _testedPanelOneCss);
	}
}

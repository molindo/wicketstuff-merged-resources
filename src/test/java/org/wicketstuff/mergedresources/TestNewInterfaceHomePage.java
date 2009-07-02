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

import org.apache.wicket.Application;
import org.apache.wicket.util.tester.WicketTester;
import org.wicketstuff.mergedresources.HomePage;


/**
 * Simple test using the WicketTester
 */
public class TestNewInterfaceHomePage extends TestCase
{
	private WicketTester tester;

	public void setUp()
	{
		tester = new WicketTester(new NewInterfaceTestApplication() {
			
			
			@Override
			protected boolean merge() {
				return true;
			}

			@Override
			protected boolean strip() {
				return true;
			}

			@Override
			public String getConfigurationType() {
				return Application.DEPLOYMENT;
			}

			
		});
	}

	public void testRenderMyPage()
	{
		assertEquals("test must run in deployment mode", tester.getApplication().getConfigurationType(), Application.DEPLOYMENT);
		
		//start and render the test page
		tester.startPage(HomePage.class);

		//assert rendered page class
		tester.assertRenderedPage(HomePage.class);
		
		assertFalse(tester.ifContains("style/all-[0-9]+\\.css").wasFailed());
		assertFalse(tester.ifContains("script/all-[0-9]+\\.js").wasFailed());
		System.out.println(tester.getServletResponse().getDocument());
		assertTrue(tester.ifContains("resources/").wasFailed());
		// does anybody know how to check resources?
	}
}

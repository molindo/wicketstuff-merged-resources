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

package org.wicketstuff.mergedresources.preprocess.css;

import junit.framework.TestCase;
import org.junit.Test;
import org.wicketstuff.mergedresources.ResourceSpec;

/**
 * Tests for {@link CssUrlRewritingResourcePreProcessor}.
 */
public class CssUrlRewritingResourcePreProcessorTest extends TestCase {

	/**
	 * Test Preprocessor helper method.
	 */
	private String testPreProcess(String cssFileName, String input) {
		CssUrlRewritingResourcePreProcessor preprocessor = new CssUrlRewritingResourcePreProcessor();
		ResourceSpec resourceSpec = new ResourceSpec(CssUrlRewritingResourcePreProcessorTest.class, cssFileName);
		String result = preprocessor.preProcess(resourceSpec, input);
		return result;
	}

	/**
	 * Test the preprocessor with empty input.
	 */
	@Test
	public void testPreProcessEmpty() {
		String actual = testPreProcess("test.css", "");
		assertEquals("", actual);
	}

	/**
	 * Test the preprocessor with a valid relative url inside a CSS file (so
	 * should be replaced).
	 */
	@Test
	public void testPreProcessValidRelativeUrl() {
		String actual = testPreProcess("test.css", "url(res/test.png);");
		String expected = "url(/resources/org.wicketstuff.mergedresources.preprocess.css.CssUrlRewritingResourcePreProcessorTest/res/test.png);";
		assertEquals(expected, actual);
	}

	/**
	 * Test the preprocessor with multiple valid relative urls inside a CSS file
	 * (so should be replaced).
	 */
	@Test
	public void testPreProcessValidRelativeMultipleUrls() {
		String actual = testPreProcess("test.css", "url(res/test.png); \n\n url(res/test2.png);");
		String expected = "url(/resources/org.wicketstuff.mergedresources.preprocess.css.CssUrlRewritingResourcePreProcessorTest/res/test.png); "
				+ "\n\n"
				+ " url(/resources/org.wicketstuff.mergedresources.preprocess.css.CssUrlRewritingResourcePreProcessorTest/res/test2.png);";
		assertEquals(expected, actual);
	}

	/**
	 * Test the preprocessor with a valid absolute url (absolute, so should not
	 * be replaced).
	 */
	@Test
	public void testPreProcessValidAbsoluteUrl() {
		String actual = testPreProcess("test.css", "url(/test.png);");
		String expected = "url(/test.png);";
		assertEquals(expected, actual);
	}

	/**
	 * Test the preprocessor with a valid relative url inside a non CSS file (so
	 * should not be replaced!).
	 */
	@Test
	public void testPreProcessValidRelativeUrlInsideJavascript() {
		String actual = testPreProcess("test.js", "url(res/test.png);");
		String expected = "url(res/test.png);";
		assertEquals(expected, actual);
	}
}

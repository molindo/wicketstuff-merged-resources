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
package org.wicketstuff.mergedresources.preprocess.css;

import org.junit.Test;
import org.wicketstuff.mergedresources.ResourceSpec;

import junit.framework.TestCase;

/**
 * Tests for {@link CssUrlRewritingResourcePreProcessor}.
 */
public class CssUrlRewritingResourcePreProcessorTest extends TestCase {

	/**
	 * Test Preprocessor helper method.
	 */
	private String testPreProcess(final String cssFileName, final String input) {
		final CssUrlRewritingResourcePreProcessor preprocessor = new CssUrlRewritingResourcePreProcessor();
		final ResourceSpec resourceSpec = new ResourceSpec(CssUrlRewritingResourcePreProcessorTest.class, cssFileName);
		final String result = preprocessor.preProcess(resourceSpec, input);
		return result;
	}

	/**
	 * Test the preprocessor with empty input.
	 */
	@Test
	public void testPreProcessEmpty() {
		final String actual = testPreProcess("test.css", "");
		assertEquals("", actual);
	}

	/**
	 * Test the preprocessor with a valid relative url inside a CSS file (so should be replaced).
	 */
	@Test
	public void testPreProcessValidRelativeUrl() {
		final String actual = testPreProcess("test.css", "url(res/test.png);");
		final String expected = "url(/resources/org.wicketstuff.mergedresources.preprocess.css.CssUrlRewritingResourcePreProcessorTest/res/test.png);";
		assertEquals(expected, actual);
	}

	/**
	 * Test the preprocessor with multiple valid relative urls inside a CSS file (so should be replaced).
	 */
	@Test
	public void testPreProcessValidRelativeMultipleUrls() {
		final String actual = testPreProcess("test.css", "url(res/test.png); \n\n url(res/test2.png);");
		final String expected = "url(/resources/org.wicketstuff.mergedresources.preprocess.css.CssUrlRewritingResourcePreProcessorTest/res/test.png); "
				+ "\n\n"
				+ " url(/resources/org.wicketstuff.mergedresources.preprocess.css.CssUrlRewritingResourcePreProcessorTest/res/test2.png);";
		assertEquals(expected, actual);
	}

	/**
	 * Test the preprocessor with a valid absolute url (absolute, so should not be replaced).
	 */
	@Test
	public void testPreProcessValidAbsoluteUrl() {
		final String actual = testPreProcess("test.css", "url(/test.png);");
		final String expected = "url(/test.png);";
		assertEquals(expected, actual);
	}

	/**
	 * Test the preprocessor with a valid relative url inside a non CSS file (so should not be replaced!).
	 */
	@Test
	public void testPreProcessValidRelativeUrlInsideJavascript() {
		final String actual = testPreProcess("test.js", "url(res/test.png);");
		final String expected = "url(res/test.png);";
		assertEquals(expected, actual);
	}
}

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
package org.wicketstuff.mergedresources.resources;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Test;

public class UncompressedCssCompressorTest {

	@Test
	public void testCompress() throws Exception {
		final String css = "a { color: #FFFFFF; }\n";

		final Charset latin1 = Charset.forName("ISO-8859-1");

		final byte[] original = css.getBytes();
		final byte[] processed = new UncompressedCssCompressor().compress(css.getBytes(latin1.name()), latin1);

		assertTrue(Arrays.toString(original) + " - " + Arrays.toString(processed), Arrays.equals(original, processed));
	}

}

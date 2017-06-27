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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class YuiCompressorUtilTest {

	@Test
	public void testCompressZeroReplacement() {

		// yui should replace 0em with 0
		{
			String toCompress = ".fooClass {\n\tfont-size: 0em;\n}\n";

			// also strips whitespace and last semicolon
			assertEquals(".fooClass{font-size:0}", YuiCompressorUtil.compress(toCompress));
		}

		// it should work however if it's 1.0em
		{
			String toCompress = ".fooClass {\n\tfont-size: 1.0em;\n}\n";

			// also strips whitespace and last semicolon
			assertEquals(".fooClass{font-size:1.0em}", YuiCompressorUtil.compress(toCompress));
		}
	}

}

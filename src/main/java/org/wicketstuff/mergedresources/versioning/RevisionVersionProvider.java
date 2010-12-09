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
package org.wicketstuff.mergedresources.versioning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RevisionVersionProvider extends AbstractClasspathResourceVersionProvider {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(RevisionVersionProvider.class);
	
	public AbstractResourceVersion getVersion(URL url) throws VersionException {
		String line = readFirstLine(url);
		
		final Matcher m = Pattern.compile("Revision: ([0-9]+)").matcher(line);
		if (m.find()) {
			final int value = Integer.valueOf(m.group(1));
			if (value <= 0) {
				throw new VersionException("found invalid resource version: "
						+ value + " in " + url);
			}
			return new SimpleResourceVersion(value);
		} else {
			throw new VersionException("did not find version in " + url);
		}
	}

	protected String readFirstLine(URL url) throws VersionException {
		String line;
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(url.openStream()));
			line = r.readLine();
		} catch (final IOException e) {
			throw new VersionException("failed to read line from url: "
					+ url, e);
		} finally {
			try {
				if (r != null) {
					r.close();
				}
			} catch (final IOException e) {
				log.warn("error while closing reader", e);
			}
		}
		return line;
	}
}

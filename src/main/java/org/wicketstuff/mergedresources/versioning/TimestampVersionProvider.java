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
package org.wicketstuff.mergedresources.versioning;

import java.io.IOException;
import java.net.URL;

public class TimestampVersionProvider extends AbstractClasspathResourceVersionProvider {

	public AbstractResourceVersion getVersion(URL url) throws VersionException {

		long timestamp = getTimestamp(url);
		if (timestamp == 0) {
			throw new VersionException("timestamp not available for " + url);
		}

		return new SimpleResourceVersion((int) ((timestamp / 1000) % Integer.MAX_VALUE));
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	protected long getTimestamp(URL url) throws VersionException {
		try {
			return url.openConnection().getLastModified();
		} catch (IOException e) {
			throw new VersionException("failed to get lastModified for " + url, e);
		}
	}

}

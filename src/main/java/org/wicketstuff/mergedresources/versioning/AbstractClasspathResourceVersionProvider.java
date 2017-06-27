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

import java.net.URL;

import org.apache.wicket.util.string.Strings;

public abstract class AbstractClasspathResourceVersionProvider implements IResourceVersionProvider {

	@Override
	public final AbstractResourceVersion getVersion(final Class<?> scope, final String file) throws VersionException {

		final URL url = toURL(scope, file);
		if (url == null) {
			throw new VersionException(scope, file, "can't find file " + file + " for scope + " + scope);
		}
		return getVersion(url);
	}

	protected abstract AbstractResourceVersion getVersion(URL url) throws VersionException;

	/**
	 * @return may return null, which will cause a VersionException
	 */
	protected URL toURL(final Class<?> scope, final String file) {

		final String path = getResourcePath(scope, file);

		URL url = scope.getClassLoader().getResource(path);
		if (url == null) {
			url = Thread.currentThread().getContextClassLoader().getResource(path);
		}
		return url;
	}

	protected String getResourcePath(final Class<?> scope, final String fileName) {
		final String file = Strings.beforeLast(scope.getName(), '.').replace('.', '/') + "/" + fileName;
		return file;
	}
}

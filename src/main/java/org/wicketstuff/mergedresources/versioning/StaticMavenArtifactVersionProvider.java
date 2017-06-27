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

import at.molindo.thirdparty.org.apache.maven.artifact.versioning.ArtifactVersion;
import at.molindo.thirdparty.org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class StaticMavenArtifactVersionProvider implements IResourceVersionProvider {

	private final MavenResourceVersion _version;

	public StaticMavenArtifactVersionProvider(final String version) {
		this(new DefaultArtifactVersion(version));
	}

	public StaticMavenArtifactVersionProvider(final ArtifactVersion version) {
		if (version == null) {
			throw new NullPointerException("version");
		}
		_version = new MavenResourceVersion(version);
	}

	@Override
	public AbstractResourceVersion getVersion(final Class<?> scope, final String file) throws VersionException {
		return _version;
	}

}

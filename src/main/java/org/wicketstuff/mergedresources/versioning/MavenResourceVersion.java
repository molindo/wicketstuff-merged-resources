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
package org.wicketstuff.mergedresources.versioning;

import at.molindo.thirdparty.org.apache.maven.artifact.versioning.ArtifactVersion;

public final class MavenResourceVersion extends AbstractResourceVersion {

	private static final long serialVersionUID = 1L;

	private final ArtifactVersion _version;

	public MavenResourceVersion(ArtifactVersion version) {
		if (version == null) {
			throw new NullPointerException("version");
		}
		_version = version;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getVersion() {
		return _version.toString();
	}

	public ArtifactVersion getArtifactVersion() {
		return _version;
	}

	@Override
	protected int compareValid(AbstractResourceVersion o) throws IncompatibleVersionsException {
		if (o instanceof MavenResourceVersion) {
			return getArtifactVersion().compareTo(((MavenResourceVersion) o).getArtifactVersion());
		} else {
			throw new IncompatibleVersionsException(this, o);
		}
	}
}
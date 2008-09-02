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

import org.apache.wicket.Application;

public class WicketVersion extends AbstractResourceVersion {
	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(WicketVersion.class);
	
	private String _version;
	
	public WicketVersion(Application application) {
		_version = application.getFrameworkSettings().getVersion();
		if ("n/a".equals(_version)) {
			log.info("failed to determine wicket framework version");
			_version = null;
		}
	}
	
	@Override
	protected int compareValid(AbstractResourceVersion o) throws IncompatibleVersionsException {
		if (o instanceof WicketVersion) {
			return 0;
		} else {
			throw new IncompatibleVersionsException(this, o);
		}
	}

	@Override
	public String getVersion() {
		return _version;
	}

	@Override
	public boolean isValid() {
		return _version != null;
	}

}

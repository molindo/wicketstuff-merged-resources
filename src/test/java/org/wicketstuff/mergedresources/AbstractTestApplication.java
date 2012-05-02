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

package org.wicketstuff.mergedresources;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy;
import org.wicketstuff.mergedresources.resources.UncompressedCssCompressor;
import org.wicketstuff.mergedresources.resources.YuiCssCompressor;

public abstract class AbstractTestApplication extends WebApplication {
	/** Constructor */
	public AbstractTestApplication() {
	}

	@Override
	protected void init() {
		getResourceSettings().setCachingStrategy(NoOpResourceCachingStrategy.INSTANCE);

		ResourceMount.setCssCompressor(this, strip() ? new YuiCssCompressor() : new UncompressedCssCompressor());
		if (merge()) {
			mountResources();
		}

		mountPage("/", getHomePage());
	}

	protected abstract void mountResources();

	protected boolean strip() {
		return true;
	}

	protected boolean merge() {
		return true;
	}

	public Class<? extends WebPage> getHomePage() {
		return HomePage.class;
	}

	@Override
	public RuntimeConfigurationType getConfigurationType() {
		return RuntimeConfigurationType.DEPLOYMENT;
	}
}

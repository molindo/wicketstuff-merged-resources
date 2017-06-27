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
package org.wicketstuff.mergedresources;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.mergedresources.resources.UncompressedCssCompressor;
import org.wicketstuff.mergedresources.resources.YuiCssCompressor;

public abstract class AbstractTestApplication extends WebApplication {
	/**
	 * Constructor
	 */
	public AbstractTestApplication() {
	}

	@Override
	protected void init() {
		ResourceMount.setCssCompressor(this, strip() ? new YuiCssCompressor() : new UncompressedCssCompressor());

		// getResourceSettings().setAddLastModifiedTimeToResourceReferenceUrl(true);

		if (merge()) {
			mountResources();
		}
	}

	protected abstract void mountResources();

	protected boolean strip() {
		return true;
	}

	protected boolean merge() {
		return true;
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage() {
		return HomePage.class;
	}

	@Override
	public String getConfigurationType() {
		return Application.DEPLOYMENT;
	}
}

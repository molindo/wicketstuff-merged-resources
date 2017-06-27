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
package org.wicketstuff.mergedresources.annotations;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.mergedresources.AbstractTestApplication;
import org.wicketstuff.mergedresources.ResourceMount;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.StaticResourceVersionProvider;

public class AnnotationTestApplication extends AbstractTestApplication {
	/**
	 * Constructor
	 */
	public AnnotationTestApplication() {
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class<? extends WebPage> getHomePage() {
		return AnnotationHomePage.class;
	}

	protected boolean merge() {
		return true;
	}

	@Override
	protected boolean strip() {
		return true;
	}

	@Override
	public String getConfigurationType() {
		return Application.DEPLOYMENT;
	}

	@Override
	protected void mountResources() {
		ResourceMount.mountWicketResources("script", this);
		ResourceMount mount = newResourceMount();
		ResourceMount.mountAnnotatedPackageResources("/files", TestAnnotationHomePage.class, this, mount);
	}

	protected ResourceMount newResourceMount() {
		IResourceVersionProvider p = new StaticResourceVersionProvider(42);
		return new ResourceMount().setResourceVersionProvider(p).setDefaultAggressiveCacheDuration();
	}
}

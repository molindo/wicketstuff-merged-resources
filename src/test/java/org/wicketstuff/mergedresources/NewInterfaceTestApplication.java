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

import org.apache.wicket.behavior.Behavior;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.PanelOne;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.StaticResourceVersionProvider;

public class NewInterfaceTestApplication extends AbstractTestApplication {

	private Behavior _jsContributor;
	private Behavior _cssPrintContributor;
	private Behavior _cssContributor;

	@Override
	protected void mountResources() {
		ResourceMount.mountWicketResources("script", this, newResourceMount());

		IResourceVersionProvider p = new StaticResourceVersionProvider(42);

		ResourceMount mount = newResourceMount().setResourceVersionProvider(p).setDefaultAggressiveCacheDuration();

		initMount(mount);

		_cssContributor = mount.clone().setPath("/style/all.css")
				.addResourceSpecsMatchingSuffix(PanelOne.class, ComponentB.class, MyForm.class).build(this);

		_cssPrintContributor = mount.clone().setPath("/style/print.css")
				.addResourceSpec(ComponentB.class, "ComponentB-print.css")
				.addResourceSpec(PanelOne.class, "PanelOne-print.css").build(this, "print");

		_jsContributor = mount.clone().setPath("/script/all.js")
				.addResourceSpecsMatchingSuffix(PanelOne.class, ComponentB.class, MyForm.class).build(this);
	}

	protected ResourceMount newResourceMount() {
		return new ResourceMount();
	}

	protected void initMount(ResourceMount mount) {
	}

	public Behavior getCssContributor() {
		if (_cssContributor == null) {
			throw new IllegalStateException("application not yet initialized");
		}
		return _cssContributor;
	}

	public Behavior getCssPrintContributor() {
		if (_cssPrintContributor == null) {
			throw new IllegalStateException("application not yet initialized");
		}
		return _cssPrintContributor;
	}

	public Behavior getJsContributor() {
		if (_jsContributor == null) {
			throw new IllegalStateException("application not yet initialized");
		}
		return _jsContributor;
	}

}

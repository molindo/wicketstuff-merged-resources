package org.wicketstuff.mergedresources;

import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.PanelOne;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.RevisionVersionProvider;

public class NewInterfaceTestApplication extends AbstractTestApplication {

	private AbstractHeaderContributor _jsContributor;
	private AbstractHeaderContributor _cssPrintContributor;
	private AbstractHeaderContributor _cssContributor;

	@Override
	protected void mountResources() {
		ResourceMount.mountWicketResources("script", this);

		IResourceVersionProvider p = new RevisionVersionProvider();

		ResourceMount mount = new ResourceMount().setResourceVersionProvider(p)
				.setDefaultAggressiveCacheDuration();

		initMount(mount);

		_cssContributor = mount.clone().setPath("/style/all.css")
				.addResourceSpecsMatchingSuffix(PanelOne.class,
						ComponentB.class, MyForm.class).build(this);

		_cssPrintContributor = mount.clone().setPath("/style/print.css")
				.addResourceSpec(ComponentB.class, "ComponentB-print.css")
				.addResourceSpec(PanelOne.class, "PanelOne-print.css").build(
						this, "print");

		_jsContributor = mount.clone().setPath("/script/all.js")
				.addResourceSpecsMatchingSuffix(PanelOne.class,
						ComponentB.class, MyForm.class).build(this);
	}

	protected void initMount(ResourceMount mount) {
	}

	public AbstractHeaderContributor getCssContributor() {
		if (_cssContributor == null) {
			throw new IllegalStateException("application not yet initialized");
		}
		return _cssContributor;
	}

	public AbstractHeaderContributor getCssPrintContributor() {
		if (_cssPrintContributor == null) {
			throw new IllegalStateException("application not yet initialized");
		}
		return _cssPrintContributor;
	}

	public AbstractHeaderContributor getJsContributor() {
		if (_jsContributor == null) {
			throw new IllegalStateException("application not yet initialized");
		}
		return _jsContributor;
	}

}

package org.wicketstuff.mergedresources;

import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.PanelOne;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.RevisionVersionProvider;

public class NewInterfaceTestApplication extends AbstractTestApplication
{    

	private AbstractHeaderContributor _jsContributor;

	@Override
	protected void mountResources() {
		ResourceMount.mountWicketResources("script", this);
		
		IResourceVersionProvider p = new RevisionVersionProvider();

		ResourceMount mount = new ResourceMount()
			.setResourceVersionProvider(p)
			.setDefaultAggressiveCacheDuration();
		
		initMount(mount);
		
		mount.clone()
			.setPath("/style/all.css")
			.addResourceSpecsMatchingSuffix(PanelOne.class, ComponentB.class, MyForm.class)
			.mount(this);

		mount.clone()
			.setPath("/style/print.css")
			.addResourceSpec(ComponentB.class, "ComponentB-print.css")
			.addResourceSpec(PanelOne.class, "PanelOne-print.css")
			.mount(this);
		
		_jsContributor = mount.clone()
			.setPath("/script/all.js")
			.addResourceSpecsMatchingSuffix(PanelOne.class, ComponentB.class, MyForm.class)
			.build(this);
	}

	protected void initMount(ResourceMount mount) {
	}

	public AbstractHeaderContributor getJsContributor() {
		if (_jsContributor == null) {
			throw new IllegalStateException("application not yet initialized");
		}
		return _jsContributor;
 	}
	
	
}

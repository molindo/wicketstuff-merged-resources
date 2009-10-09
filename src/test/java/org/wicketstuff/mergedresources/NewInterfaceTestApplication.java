package org.wicketstuff.mergedresources;

import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.PanelOne;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.RevisionVersionProvider;

public class NewInterfaceTestApplication extends AbstractTestApplication
{    

	@Override
	protected void mountResources() {
		ResourceMount.mountWicketResources("script", this);
		
		IResourceVersionProvider p = new RevisionVersionProvider();

		ResourceMount mount = new ResourceMount()
			.setResourceVersionProvider(p)
			.setDefaultAggressiveCacheDuration();
		
		mount.clone()
			.setPath("/style/all.css")
			.addResourceSpecsMatchingSuffix(PanelOne.class, ComponentB.class, MyForm.class)
			.mount(this);

		mount.clone()
			.setPath("/script/all.js")
			.addResourceSpecsMatchingSuffix(PanelOne.class, ComponentB.class, MyForm.class)
			.mount(this);
	}

}

package org.wicketstuff.mergedresources;

import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.PanelOne;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.RevisionVersionProvider;


/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class NewInterfaceTestApplication extends WebApplication
{    
    /**
     * Constructor
     */
	public NewInterfaceTestApplication()
	{
	}
	
	
	
	@Override
	protected void init() {
		getResourceSettings().setStripJavascriptCommentsAndWhitespace(strip());
		//getResourceSettings().setAddLastModifiedTimeToResourceReferenceUrl(true);
		
		if (merge()) {
			ResourceMount.mountWicketResources("script", this);
			
			IResourceVersionProvider p = new RevisionVersionProvider();

			ResourceMount mount = new ResourceMount()
				.setResourceVersionProvider(p)
				.setDefaultAggresiveCacheDuration();
			
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

	protected boolean strip() {
		return true;
	}

	protected boolean merge() {
		return true;
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class<?> getHomePage()
	{
		return HomePage.class;
	}

}

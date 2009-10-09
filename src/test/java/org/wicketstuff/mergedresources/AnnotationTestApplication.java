package org.wicketstuff.mergedresources;

import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.RevisionVersionProvider;


/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class AnnotationTestApplication extends WebApplication
{    
	public static final String ALL_JS = "all.js";
	public static final String ALL_CSS = "/css/all.css";
	
    /**
     * Constructor
     */
	public AnnotationTestApplication()
	{
	}
	
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void init() {
		// still using deprecated property for CSS
		getResourceSettings().setStripJavascriptCommentsAndWhitespace(strip());
		
		//getResourceSettings().setAddLastModifiedTimeToResourceReferenceUrl(true);
		
		if (merge()) {
			ResourceMount.mountWicketResources("script", this);
			
			IResourceVersionProvider p = new RevisionVersionProvider();

			ResourceMount mount = new ResourceMount()
				.setResourceVersionProvider(p)
				.setDefaultAggressiveCacheDuration();
			
			ResourceMount.mountAnnotatedPackageResources(mount, "/r", this.getClass(), this);
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
	public Class<HomePage> getHomePage()
	{
		return HomePage.class;
	}

}

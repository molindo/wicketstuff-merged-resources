package org.wicketstuff.mergedresources.annotations;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.mergedresources.AbstractTestApplication;
import org.wicketstuff.mergedresources.ResourceMount;
import org.wicketstuff.mergedresources.versioning.IResourceVersionProvider;
import org.wicketstuff.mergedresources.versioning.RevisionVersionProvider;

public class AnnotationTestApplication extends AbstractTestApplication
{    
    /**
     * Constructor
     */
	public AnnotationTestApplication()
	{
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class<? extends WebPage> getHomePage()
	{
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
		IResourceVersionProvider p = new RevisionVersionProvider();
		return new ResourceMount()
			.setResourceVersionProvider(p)
			.setDefaultAggressiveCacheDuration();
	}
}

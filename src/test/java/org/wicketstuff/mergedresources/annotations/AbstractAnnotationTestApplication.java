package org.wicketstuff.mergedresources.annotations;

import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.mergedresources.AbstractTestApplication;

public abstract class AbstractAnnotationTestApplication extends AbstractTestApplication
{    
    /**
     * Constructor
     */
	public AbstractAnnotationTestApplication()
	{
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class<? extends WebPage> getHomePage()
	{
		return AnnotationHomePage.class;
	}
}

package org.wicketstuff.mergedresources.util;

import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;

public class WicketResourceTester extends WicketTester {

	public WicketResourceTester() {
		super();
	}

	public WicketResourceTester(Class<? extends Page> homePage) {
		super(homePage);
	}

	public WicketResourceTester(WebApplication application, String path) {
		super(application, path);
	}

	public WicketResourceTester(WebApplication application) {
		super(application);
	}

	public String urlFor(ResourceReference resourceReference) {
		CharSequence cs = resolveRequestCycle().urlFor(resourceReference);
		return cs == null ? null : cs.toString();
	}
}

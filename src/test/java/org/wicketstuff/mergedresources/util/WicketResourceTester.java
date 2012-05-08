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

package org.wicketstuff.mergedresources.util;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.ResourceReference;
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
		CharSequence cs = getRequestCycle().urlFor(resourceReference, null);
		return cs == null ? null : cs.toString();
	}
}

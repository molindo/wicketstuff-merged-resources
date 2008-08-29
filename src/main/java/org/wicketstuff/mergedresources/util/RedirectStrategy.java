/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.mergedresources.util;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy;

public class RedirectStrategy extends
		BookmarkablePageRequestTargetUrlCodingStrategy {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(RedirectStrategy.class);

	private final String _redirectPath;

	public RedirectStrategy(final String mountPath, final String pageMapName, final String redirectPath) {
		super(mountPath, WebPage.class, pageMapName);
		_redirectPath = redirectPath;
	}

	public RedirectStrategy(String mountPath, String redirectPath) {
		this(mountPath, null, redirectPath);
	}

	@Override
	public IRequestTarget decode(final RequestParameters requestParameters) {
		if (log.isDebugEnabled()) {
			final WebRequest r = (WebRequest) RequestCycle.get().getRequest();
			final String ref = r.getHttpServletRequest().getHeader("Referer");
			final String requested = r.getURL();
			log.debug("redirecting request coming from " + ref + " to "
					+ requested + " to " + _redirectPath);
		}

		return new RedirectRequestTarget(_redirectPath);
	}

}

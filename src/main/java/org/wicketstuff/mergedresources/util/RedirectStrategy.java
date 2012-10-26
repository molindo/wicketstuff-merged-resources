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

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;

public class RedirectStrategy extends MountedMapper {

	private final String redirectUrl;

	public RedirectStrategy(final String mountPath, final String redirectPath) {
		super(mountPath, WebPage.class);
		this.redirectUrl = redirectPath;
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		if (super.mapRequest(request) == null) {
			return null;
		}
		return new RedirectRequestHandler(redirectUrl);
	}

}

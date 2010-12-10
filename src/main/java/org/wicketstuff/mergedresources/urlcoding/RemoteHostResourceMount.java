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

package org.wicketstuff.mergedresources.urlcoding;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.wicketstuff.mergedresources.ResourceMount;

import at.molindo.utils.data.StringUtils;

public class RemoteHostResourceMount extends ResourceMount {
	private final URL _root;

	public RemoteHostResourceMount(String root) throws MalformedURLException {
		this(new URL(root));
	}

	public RemoteHostResourceMount(URL root) {
		try {
			_root = new URL(StringUtils.trailing(root.toString(), "/"));
		} catch (MalformedURLException e) {
			throw new RuntimeException("failed to append / to url " + root, e);
		}
	}

	@Override
	protected IRequestTargetUrlCodingStrategy newStrategy(String mountPath, final ResourceReference ref,
			final boolean merge) {

		return new RemoteHostUrlCodingStrategy(_root, mountPath, ref) {
			@Override
			protected SharedResourceRequestTargetUrlCodingStrategy newStrategy(final String mountPath,
					final String sharedResourceKey) {
				return (SharedResourceRequestTargetUrlCodingStrategy) RemoteHostResourceMount.super.newStrategy(
						mountPath, ref, merge);
			}
		};
	}
}
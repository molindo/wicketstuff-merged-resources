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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.mapper.ResourceMapper;
import org.apache.wicket.request.resource.ResourceReference;
import org.wicketstuff.mergedresources.ResourceMount;

import java.net.MalformedURLException;
import java.net.URL;

public class RemoteHostResourceMount extends ResourceMount {
	private final URL _root;
	private final boolean _enabled;

	private static URL toURL(String root) {
		try {
			return root == null ? null : new URL(root);
		} catch (MalformedURLException e) {
			throw new WicketRuntimeException(e);
		}
	}

	public RemoteHostResourceMount(String root) throws WicketRuntimeException {
		this(toURL(root), true);
	}

	public RemoteHostResourceMount(String root, boolean enabled) throws WicketRuntimeException {
		this(toURL(root), enabled);
	}

	public RemoteHostResourceMount(URL root) {
		this(root, true);
	}

	public RemoteHostResourceMount(URL root, boolean enabled) {
		_enabled = enabled;
		_root = root;
	}

	@Override
	protected IRequestMapper newStrategy(String mountPath, final ResourceReference ref, final boolean merge) {
		if (!_enabled) {
			return super.newStrategy(mountPath, ref, merge);
		} else {
			return new RemoteHostUrlCodingStrategy(_root, mountPath, ref) {
				@Override
				protected ResourceMapper newStrategy(final String mountPath, final ResourceReference sharedResourceKey) {
					return (ResourceMapper) RemoteHostResourceMount.super.newStrategy(mountPath, ref, merge);
				}
			};
		}
	}
}
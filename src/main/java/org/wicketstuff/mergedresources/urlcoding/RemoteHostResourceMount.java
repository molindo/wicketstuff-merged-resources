/**
 * Copyright 2016 Molindo GmbH
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
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.wicketstuff.mergedresources.ResourceMount;

public class RemoteHostResourceMount extends ResourceMount {
	private final URL _root;
	private final boolean _enabled;
	private final boolean _schemeless;

	private static URL toURL(final String root) {
		try {
			return root == null ? null : new URL(root);
		} catch (final MalformedURLException e) {
			throw new WicketRuntimeException(e);
		}
	}

	public RemoteHostResourceMount(final String root) throws WicketRuntimeException {
		this(toURL(root), true);
	}

	public RemoteHostResourceMount(final String root, final boolean enabled) throws WicketRuntimeException {
		this(toURL(root), enabled);
	}

	public RemoteHostResourceMount(final String root, final boolean enabled, final boolean schemeless) throws WicketRuntimeException {
		this(toURL(root), enabled, schemeless);
	}

	public RemoteHostResourceMount(final URL root) {
		this(root, true);
	}

	public RemoteHostResourceMount(final URL root, final boolean enabled) {
		this(root, enabled, false);
	}

	public RemoteHostResourceMount(final URL root, final boolean enabled, final boolean schemeless) {
		_enabled = enabled;
		_root = root;
		_schemeless = schemeless;
	}

	@Override
	protected IRequestTargetUrlCodingStrategy newStrategy(final String mountPath, final ResourceReference ref, final boolean merge) {
		if (!_enabled) {
			return super.newStrategy(mountPath, ref, merge);
		} else {
			return new RemoteHostUrlCodingStrategy(_root, mountPath, ref) {
				@Override
				protected SharedResourceRequestTargetUrlCodingStrategy newStrategy(final String mountPath, final String sharedResourceKey) {
					return (SharedResourceRequestTargetUrlCodingStrategy) RemoteHostResourceMount.super.newStrategy(mountPath, ref, merge);
				}
			}.setUseSchemelessUrl(_schemeless);
		}
	}
}

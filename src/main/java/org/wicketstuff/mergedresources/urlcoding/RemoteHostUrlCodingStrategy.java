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

import java.net.URL;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.IMountableRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.resource.SharedResourceRequestTarget;

import at.molindo.utils.data.StringUtils;

public class RemoteHostUrlCodingStrategy implements IRequestTargetUrlCodingStrategy,
		IMountableRequestTargetUrlCodingStrategy {

	// private static final org.slf4j.Logger log =
	// org.slf4j.LoggerFactory.getLogger(RemoteHostUrlCodingStrategy.class);

	private final SharedResourceRequestTargetUrlCodingStrategy _strategy;
	private final String _key;

	private final String _root;

	public RemoteHostUrlCodingStrategy(URL root, final String mountPath, final ResourceReference ref) {
		if (ref == null) {
			throw new NullPointerException("sharedResourceKey");
		}
		_key = ref.getSharedResourceKey();
		_strategy = newStrategy(mountPath, _key);

		_root = StringUtils.trailing(root.toString(), "/");
	}

	protected SharedResourceRequestTargetUrlCodingStrategy newStrategy(final String mountPath,
			final String sharedResourceKey) {
		return new SharedResourceRequestTargetUrlCodingStrategy(mountPath, sharedResourceKey);
	}

	@Override
	public IRequestTarget decode(final RequestParameters requestParameters) {
		return new IRequestTarget() {

			private SharedResourceRequestTarget _orig;

			private SharedResourceRequestTarget getOriginalRequestTarget() {
				if (_orig == null) {
					_orig = (SharedResourceRequestTarget) _strategy.decode(requestParameters);
				}
				return _orig;
			}

			@Override
			public void detach(final RequestCycle requestCycle) {
				if (_orig != null) {
					_orig.detach(requestCycle);
				}
			}

			@Override
			public void respond(final RequestCycle requestCycle) {
				getOriginalRequestTarget().respond(requestCycle);
			}
		};
	}

	@Override
	public CharSequence encode(final IRequestTarget requestTarget) {
		return _root + StringUtils.stripLeading(_strategy.encode(requestTarget).toString(), "/");
	}

	@Override
	public String getMountPath() {
		return _strategy.getMountPath();
	}

	@Override
	public boolean matches(final IRequestTarget requestTarget) {
		return _strategy.matches(requestTarget);
	}

	@Override
	public boolean matches(final String path, final boolean b) {
		return _strategy.matches(path, b);
	}

}

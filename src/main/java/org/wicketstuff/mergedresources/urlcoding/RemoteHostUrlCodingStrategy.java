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

import at.molindo.utils.data.StringUtils;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.apache.wicket.request.mapper.ResourceMapper;
import org.apache.wicket.request.resource.ResourceReference;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

public class RemoteHostUrlCodingStrategy extends AbstractMapper {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RemoteHostUrlCodingStrategy.class);

	private final ResourceMapper _strategy;

	private final String _protocol;
	private final Integer _port;
	private final String _host;
	private final String _path;
	private boolean _useRequestProtocol = true;
	private boolean _useRequestPort = true;

	public RemoteHostUrlCodingStrategy(URL root, final String mountPath, final ResourceReference ref) {
		if (ref == null) {
			throw new NullPointerException("sharedResourceKey");
		}
		_strategy = newStrategy(mountPath, ref);

		if (root != null) {
			_protocol = root.getProtocol();
			_port = root.getPort();
			_host = root.getHost();
			_path = StringUtils.trailing(root.getFile(), "/");
		} else {
			_port = null;
			_host = _protocol = _path = null;
		}
	}

	protected ResourceMapper newStrategy(final String mountPath, final ResourceReference sharedResourceKey) {
		return new ResourceMapper(mountPath, sharedResourceKey);
	}

	@Override
	public IRequestHandler mapRequest(final Request request) {
		return new IRequestHandler() {

			private IRequestHandler _orig;

			@Override
			public void detach(final IRequestCycle requestCycle) {
				if (_orig != null) {
					_orig.detach(requestCycle);
				}
			}

			@Override
			public void respond(final IRequestCycle requestCycle) {
				getOriginalRequestTarget().respond(requestCycle);
			}

			private IRequestHandler getOriginalRequestTarget() {
				if (_orig == null) {
					_orig = _strategy.mapRequest(request);
				}
				return _orig;
			}
		};
	}

	@Override
	public int getCompatibilityScore(Request request) {
		return 0;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		final Url encoded = _strategy.mapHandler(requestHandler);
		if (_host == null) {
			return encoded;
		}
		if (encoded == null) {
			return null;
		}

		final HttpServletRequest serlvetRequest = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
		String protocol = !isUseRequestProtocol() ? _protocol : serlvetRequest.getScheme();
		Integer port = !isUseRequestPort() ? _port : serlvetRequest.getServerPort();
		if (port != null) {
			if (port == 80 && "http".equals(protocol)) {
				port = null;
			} else if (port == 443 && "https".equals(protocol)) {
				port = null;
			}
		}

		try {
			return Url.parse(new URL(protocol, _host, port == null ? -1 : port, _path + StringUtils.stripLeading(encoded.toString(), "/")).toString());
		} catch (MalformedURLException e) {
			log.error("failed to build URL, balling back to default", e);
			return encoded;
		}
	}

	public boolean isUseRequestProtocol() {
		return _useRequestProtocol;
	}

	public RemoteHostUrlCodingStrategy setUseRequestProtocol(boolean useRequestProtocol) {
		_useRequestProtocol = useRequestProtocol;
		return this;
	}

	public boolean isUseRequestPort() {
		return _useRequestPort;
	}

	public RemoteHostUrlCodingStrategy setUseRequestPort(boolean useRequestPort) {
		_useRequestPort = useRequestPort;
		return this;
	}

}

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

import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;

import java.util.List;

public class MergedResourceMapper extends ResourceMapper {

	private final IPageParametersEncoder parametersEncoder;
	private final String[] mountSegments;
	private final ResourceReference resourceReference;
	private final List<String> mergedKeys;

	public MergedResourceMapper(String mountPath, ResourceReference ref, List<String> mergedKeys) {
		this(mountPath, ref, new PageParametersEncoder(), mergedKeys);
	}

	private MergedResourceMapper(String path, ResourceReference resourceReference, IPageParametersEncoder encoder, List<String> mergedKeys) {
		super(path, resourceReference, encoder);

		this.mergedKeys = mergedKeys;
		this.resourceReference = resourceReference;
		mountSegments = getMountSegments(path);
		parametersEncoder = encoder;
	}

	@Override
	public int getCompatibilityScore(Request request) {
		if (urlStartsWith(request.getUrl(), mountSegments)) {
			return mountSegments.length;
		} else {
			return 0;
		}
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		return super.mapRequest(request);
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		if (!(requestHandler instanceof ResourceReferenceRequestHandler)) {
			return null;
		}

		ResourceReferenceRequestHandler handler = (ResourceReferenceRequestHandler) requestHandler;

		// see if request handler addresses the resource reference we serve
		if (!handlesRequest(handler)) { return null; }

		Url url = new Url();

		// add mount path segments
		for (String segment : mountSegments) {
			url.getSegments().add(segment);
		}

		// replace placeholder parameters
		PageParameters parameters = new PageParameters(handler.getPageParameters());

		for (int index = 0; index < mountSegments.length; ++index) {
			String placeholder = getPlaceholder(mountSegments[index]);

			if (placeholder != null) {
				url.getSegments().set(index, parameters.get(placeholder).toString(""));
				parameters.remove(placeholder);
			}
		}

		// add caching information
		addCachingDecoration(url, parameters);

		// create url
		return encodePageParameters(url, parameters, parametersEncoder);
	}

	protected boolean handlesRequest(ResourceReferenceRequestHandler handler) {
		return resourceReference.equals(handler.getResourceReference()) ||
				mergedKeys.contains(handler.getResourceReference().toString());
	}

}

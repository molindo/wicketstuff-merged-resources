/**
 * 
 */
package org.wicketstuff.mergedresources.util;

import java.util.ArrayList;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.resource.ISharedResourceRequestTarget;

public final class MergedResourceRequestTargetUrlCodingStrategy extends SharedResourceRequestTargetUrlCodingStrategy {
	private final ArrayList<String> _mergedKeys;

	public MergedResourceRequestTargetUrlCodingStrategy(String mountPath, String resourceKey, ArrayList<String> mergedKeys) {
		super(mountPath, resourceKey);
		_mergedKeys = mergedKeys;
	}

	@Override
	public boolean matches(final IRequestTarget requestTarget) {
		if (requestTarget instanceof ISharedResourceRequestTarget) {
			final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget) requestTarget;
			return super.matches(requestTarget)
					|| _mergedKeys.contains(target.getResourceKey());
		} else {
			return false;
		}
	}
}
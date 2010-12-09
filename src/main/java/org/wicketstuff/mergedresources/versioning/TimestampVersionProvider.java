package org.wicketstuff.mergedresources.versioning;

import java.io.IOException;
import java.net.URL;

public class TimestampVersionProvider extends AbstractClasspathResourceVersionProvider {
	
	public AbstractResourceVersion getVersion(URL url) throws VersionException {
		
		long timestamp = getTimestamp(url);
		if (timestamp == 0) {
			throw new VersionException("timestamp not available for " + url);
		}
		
		return new SimpleResourceVersion((int) ((timestamp / 1000) % Integer.MAX_VALUE));
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	protected long getTimestamp(URL url) throws VersionException {
		try {
			return url.openConnection().getLastModified();
		} catch (IOException e) {
			throw new VersionException("failed to get lastModified for " + url, e);
		}
	}

}

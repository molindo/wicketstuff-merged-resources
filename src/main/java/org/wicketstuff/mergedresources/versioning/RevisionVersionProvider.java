package org.wicketstuff.mergedresources.versioning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.string.Strings;

public class RevisionVersionProvider implements IResourceVersionProvider {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(RevisionVersionProvider.class);
	
	public int getVersion(final Class<?> scope, final String fileName) throws VersionException {
		final String file = getResourcePath(scope, fileName);
		final InputStream in = ClassLoader.getSystemResourceAsStream(file);
		if (in == null) {
			throw new VersionException(scope, fileName, "can't find " + file);
		}
		final BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			line = r.readLine();
		} catch (final IOException e) {
			throw new VersionException(scope, fileName, "failed to read line from file: "
					+ file, e);
		} finally {
			try {
				r.close();
			} catch (final IOException e) {
				log.warn("error while closing reader", e);
			}
		}
		final Matcher m = Pattern.compile("Revision: ([0-9]+)").matcher(line);
		if (m.find()) {
			final int value = Integer.valueOf(m.group(1));
			if (value <= 0) {
				throw new VersionException(scope, fileName, "found invalid resource version: "
						+ value);
			}
			return value;
		} else {
			throw new VersionException(scope, fileName, "did not find version in " + file);
		}
	}
	
	protected String getResourcePath(final Class<?> scope, final String fileName) {
		final String file = Strings.beforeLast(scope.getName(), '.').replace('.', '/') + "/"
				+ fileName;
		return file;
	}
}

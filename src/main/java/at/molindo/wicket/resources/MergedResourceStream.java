/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.molindo.wicket.resources;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.IClusterable;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.locator.ResourceNameIterator;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.watch.ModificationWatcher;

public class MergedResourceStream implements IResourceStream {
	private static final long serialVersionUID = 1L;
	private static transient final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(MergedResourceStream.class);

	private final Class<?>[] _scopes;
	private final String[] _files;
	private Locale _locale;
	private final String _style;
	private LocalizedMergedResourceStream _localizedMergedResourceStream;

	public MergedResourceStream(final Class<?>[] scopes, final String[] files, final Locale locale, final String style) {
		_scopes = scopes.clone();
		_files = files.clone();
		_locale = locale;
		_style = style;
	}

	public void close() throws IOException {
		if (_localizedMergedResourceStream != null) {
			getLocalizedMergedResourceStream().close();
			_localizedMergedResourceStream = null;
		}
	}

	public String getContentType() {
		return null;
	}

	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		return getLocalizedMergedResourceStream().getInputStream();
	}

	public Locale getLocale() {
		return _locale;
	}

	public long length() {
		return getLocalizedMergedResourceStream().getContent().length();
	}

	public void setLocale(final Locale locale) {
		_locale = locale;
	}

	public Time lastModifiedTime() {
		return getLocalizedMergedResourceStream().getLastModifiedTime();
	}

	private LocalizedMergedResourceStream getLocalizedMergedResourceStream() {
		if (_localizedMergedResourceStream == null) {
			_localizedMergedResourceStream = new LocalizedMergedResourceStream();
		}
		return _localizedMergedResourceStream;
	}

	private final class LocalizedMergedResourceStream implements IClusterable {
		private static final long serialVersionUID = 1L;
		private final String _content;
		private final Time _lastModifiedTime;

		private LocalizedMergedResourceStream() {
			Time max = null;
			final StringWriter w = new StringWriter(4096);
			final ArrayList<IResourceStream> resourceStreams = new ArrayList<IResourceStream>(_scopes.length);

			for (int i = 0; i < _scopes.length; i++) {
				final Class<?> scope = _scopes[i];
				final String fileName = _files[i];

				final IResourceStream resourceStream = findResourceStream(scope, fileName);
				try {

					final Time lastModified = resourceStream.lastModifiedTime();
					if (max == null || lastModified != null && lastModified.after(max)) {
						max = lastModified;
					}
					writeContent(w, resourceStream);
					resourceStreams.add(resourceStream);
				} catch (final IOException e) {
					throw new WicketRuntimeException("failed to read from " + resourceStream, e);
				} catch (final ResourceStreamNotFoundException e) {
					throw new WicketRuntimeException("did not find resource", e);
				} finally {
					try {
						if (resourceStream != null) {
							resourceStream.close();
						}
					} catch (final IOException e) {
						log.warn("error while closing reader", e);
					}
				}

			}
			_content = toContent(w.toString());
			_lastModifiedTime = max == null ? Time.now() : max;
			watchForChanges(resourceStreams);
		}

		private IResourceStream findResourceStream(final Class<?> scope, final String fileName) {
			// Create the base path
			final String path = Strings.beforeLast(scope.getName(), '.').replace('.', '/') + '/'
					+ Strings.beforeLast(fileName, '.');
			// Iterator over all the combinations
			final ResourceNameIterator iter = new ResourceNameIterator(path, _style, _locale, Strings
					.afterLast(fileName, '.'));

			IResourceStream resourceStream = null;
			while (resourceStream == null && iter.hasNext()) {
				final String resourceName = (String) iter.next();
				resourceStream = Application.get().getResourceSettings().getResourceStreamLocator()
						.locate(scope, resourceName, _style, _locale, null);
			}

			if (resourceStream == null) {
				throw new WicketRuntimeException("did not find IResourceStream for "
						+ Arrays.asList(scope, fileName, _style, _locale));
			}

			return resourceStream;
		}

		private void writeContent(final StringWriter w, final IResourceStream resourceStream) throws ResourceStreamNotFoundException, IOException {
			// open reader
			final BufferedReader r = new BufferedReader(new InputStreamReader(resourceStream
					.getInputStream()));
			String line;
			while ((line = r.readLine()) != null) {
				// write
				w.write(line);
				w.write("\n");
			}
			w.write("\n\n");
		}

		private void watchForChanges(final List<IResourceStream> resourceStreams) {
			// Watch file in the future
			final ModificationWatcher watcher = Application.get().getResourceSettings()
					.getResourceWatcher(true);
			if (watcher != null) {
				final IChangeListener listener = new IChangeListener() {
					public void onChange() {
						log.info("merged resource has changed");
						for (final IResourceStream resourceStream : resourceStreams) {
							watcher.remove(resourceStream);
						}
						_localizedMergedResourceStream = null;
					}
				};
				for (final IResourceStream resourceStream : resourceStreams) {
					watcher.add(resourceStream, listener);
				}
			}
		}

		public void close() throws IOException {

		}

		public InputStream getInputStream() {
			return new ByteArrayInputStream(getContent().getBytes());
		}

		public String getContent() {
			return _content;
		}

		public Time getLastModifiedTime() {
			return _lastModifiedTime;
		}
	}

	protected String toContent(final String content) {
		return content;
	}
}

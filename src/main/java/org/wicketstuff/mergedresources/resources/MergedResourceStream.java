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
package org.wicketstuff.mergedresources.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.IClusterable;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.locator.ResourceNameIterator;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.watch.IModificationWatcher;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

public class MergedResourceStream implements IResourceStream {
	private static final long serialVersionUID = 1L;
	private static transient final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(MergedResourceStream.class);

	private final ResourceSpec[] _specs;
	private Locale _locale;
	private final String _style;
	private LocalizedMergedResourceStream _localizedMergedResourceStream;
	private IResourcePreProcessor _preProcessor;

	/**
	 * @deprecated use ResourceSpec[] instead of scopes[] and files[]
	 */
	@Deprecated
	public MergedResourceStream(final Class<?>[] scopes, final String[] files, final Locale locale, final String style) {
		this(ResourceSpec.toResourceSpecs(scopes, files), locale, style, null);
	}

	public MergedResourceStream(final ResourceSpec[] specs, final Locale locale, final String style, IResourcePreProcessor preProcessor) {
		_specs = specs.clone();
		_locale = locale;
		_style = style;
		_preProcessor = preProcessor;
	}
	
	public void close() throws IOException {
		// do nothing
	}

	public String getContentType() {
		return getLocalizedMergedResourceStream().getContentType();
	}

	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		return getLocalizedMergedResourceStream().getInputStream();
	}

	public Locale getLocale() {
		return _locale;
	}

	public long length() {
		return getLocalizedMergedResourceStream().getContent().length;
	}

	public void setLocale(final Locale locale) {
		_locale = locale;
	}

	public Time lastModifiedTime() {
		return getLocalizedMergedResourceStream().getLastModifiedTime();
	}

	private LocalizedMergedResourceStream getLocalizedMergedResourceStream() {
		if (_localizedMergedResourceStream == null) {
			synchronized (this) {
				if (_localizedMergedResourceStream == null) {
					_localizedMergedResourceStream = new LocalizedMergedResourceStream();
				}
			}
		}
		return _localizedMergedResourceStream;
	}

	private final class LocalizedMergedResourceStream implements IClusterable {
		private static final long serialVersionUID = 1L;
		private final byte[] _content;
		private final String _contentType;
		private final Time _lastModifiedTime;

		private LocalizedMergedResourceStream() {
			Time max = null;
			//final StringWriter w = new StringWriter(4096);
			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
			
			final ArrayList<IResourceStream> resourceStreams = new ArrayList<IResourceStream>(_specs.length);

			String contentType = null;
			for (int i = 0; i < _specs.length; i++) {			
				final Class<?> scope = _specs[i].getScope();
				final String fileName = _specs[i].getFile();


				
				final IResourceStream resourceStream = findResourceStream(scope, fileName);
				if (contentType != null) {
					if (resourceStream.getContentType() != null && !contentType.equalsIgnoreCase(resourceStream.getContentType())) {
						log.warn("content types of merged resources don't match: '" + resourceStream.getContentType() + "' and '" + contentType + "'");
					}
				} else {
					contentType = resourceStream.getContentType();
				}
				
				try {

					final Time lastModified = resourceStream.lastModifiedTime();
					if (max == null || lastModified != null && lastModified.after(max)) {
						max = lastModified;
					}
					if (i > 0) {
						writeFileSeparator(out);
					}
					writeContent(out, resourceStream);
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
			_contentType = contentType;
			
			_content = toContent(preProcess(out.toByteArray()));
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

		private void writeContent(final OutputStream out, final IResourceStream resourceStream) throws ResourceStreamNotFoundException, IOException {
			Streams.copy(resourceStream.getInputStream(), out);
			out.flush();
		}

		private void writeFileSeparator(ByteArrayOutputStream out) throws IOException {
			out.write(getFileSeparator());
		}
		
		private byte[] getFileSeparator() {
			return isPlainText() ? "\n\n".getBytes() : new byte[0];
		}

		private void watchForChanges(final List<IResourceStream> resourceStreams) {
			// Watch file in the future
			final IModificationWatcher watcher = Application.get().getResourceSettings()
					.getResourceWatcher(true);
			if (watcher != null) {
				final IChangeListener listener = new IChangeListener() {
					public void onChange() {
						log.info("merged resource has changed");
						synchronized (MergedResourceStream.this) {
							for (final IResourceStream resourceStream : resourceStreams) {
								watcher.remove(resourceStream);
							}
							_localizedMergedResourceStream = null;
						}
					}
				};
				for (final IResourceStream resourceStream : resourceStreams) {
					watcher.add(resourceStream, listener);
				}
			}
		}

		public InputStream getInputStream() {
			return new ByteArrayInputStream(getContent());
		}

		public byte[] getContent() {
			return _content;
		}

		public Time getLastModifiedTime() {
			return _lastModifiedTime;
		}

		public String getContentType() {
			return _contentType;
		}
	}

	protected byte[] toContent(final byte[] content) {
		return content;
	}

	public boolean isPlainText() {
		// TODO maybe something smarter?
		return true;
	}

	public byte[] preProcess(byte[] content) {
		return (_preProcessor != null) ? _preProcessor.preProcess(content) : content;
	}
}

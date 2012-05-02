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

package org.wicketstuff.mergedresources.resources;

import at.molindo.utils.io.StreamUtils;
import org.apache.wicket.Application;
import org.apache.wicket.IClusterable;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.locator.ResourceNameIterator;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.watch.IModificationWatcher;
import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.IResourcePreProcessor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MergedResourceStream implements IResourceStream {
	private static final long serialVersionUID = 1L;
	private static transient final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MergedResourceStream.class);

	private final ResourceSpec[] _specs;
	private Locale _locale;
	private String _style;
	private String _variation;

	private LocalizedMergedResourceStream _localizedMergedResourceStream;
	private final IResourcePreProcessor _preProcessor;

	public MergedResourceStream(final ResourceSpec[] specs, final Locale locale, final String style, IResourcePreProcessor preProcessor) {
		_specs = specs.clone();
		_locale = locale;
		_style = style;
		_variation = null;
		_preProcessor = preProcessor;
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}

	@Override
	public String getContentType() {
		return getLocalizedMergedResourceStream().getContentType();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		return getLocalizedMergedResourceStream().getInputStream();
	}

	@Override
	public Locale getLocale() {
		return _locale;
	}

	@Override
	public Bytes length() {
		return Bytes.bytes(getLocalizedMergedResourceStream().getContent().length);
	}

	@Override
	public void setLocale(final Locale locale) {
		_locale = locale;
	}

	@Override
	public String getStyle() {
		return _style;
	}

	@Override
	public void setStyle(String style) {
		this._style = style;
	}

	@Override
	public String getVariation() {
		return _variation;
	}

	@Override
	public void setVariation(String variation) {
		this._variation = variation;
	}

	@Override
	public Time lastModifiedTime() {
		return getLocalizedMergedResourceStream().getLastModifiedTime();
	}

	private LocalizedMergedResourceStream getLocalizedMergedResourceStream() {
		synchronized (this) {
			if (_localizedMergedResourceStream == null) {
				_localizedMergedResourceStream = new LocalizedMergedResourceStream();
			}
			return _localizedMergedResourceStream;
		}
	}

	private final class LocalizedMergedResourceStream implements IClusterable {
		private static final long serialVersionUID = 1L;

		private final byte[] _content;
		private final String _contentType;
		private final Time _lastModifiedTime;

		private LocalizedMergedResourceStream() {
			Time max = null;
			// final StringWriter w = new StringWriter(4096);
			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);

			final ArrayList<IResourceStream> resourceStreams = new ArrayList<IResourceStream>(_specs.length);

			String contentType = null;
			for (int i = 0; i < _specs.length; i++) {
				final Class<?> scope = _specs[i].getScope();
				final String fileName = _specs[i].getFile();

				final IResourceStream resourceStream = findResourceStream(scope, fileName);
				if (contentType != null) {
					if (resourceStream.getContentType() != null
							&& !contentType.equalsIgnoreCase(resourceStream.getContentType())) {
						log.warn("content types of merged resources don't match: '" + resourceStream.getContentType()
								+ "' and '" + contentType + "'");
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
					// process content from single spec
					byte[] preprocessed = preProcess(_specs[i], StreamUtils.bytes(resourceStream.getInputStream()));
					writeContent(out, new ByteArrayInputStream(preprocessed));
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
			_content = toContent(preProcess(null, out.toByteArray()));
			_lastModifiedTime = max == null ? Time.now() : max;
			watchForChanges(resourceStreams);
		}

		private IResourceStream findResourceStream(final Class<?> scope, final String fileName) {
			// Create the base path
			final String path = Strings.beforeLast(scope.getName(), '.').replace('.', '/') + '/'
					+ Strings.beforeLast(fileName, '.');
			// Iterator over all the combinations
			final ResourceNameIterator iter = new ResourceNameIterator(path, _style, _variation, _locale, Strings.afterLast(fileName, '.'), false);
			IResourceStream resourceStream = null;
			while (resourceStream == null && iter.hasNext()) {
				final String resourceName = iter.next();
				resourceStream = Application.get().getResourceSettings().getResourceStreamLocator()
						.locate(scope, resourceName, _style, _variation, _locale, null, false);
			}

			if (resourceStream == null) {
				throw new WicketRuntimeException("did not find IResourceStream for " + Arrays.asList(scope, fileName, _style, _locale));
			}

			return resourceStream;
		}

		private void writeContent(final OutputStream out, final InputStream resourceStream) throws IOException {
			StreamUtils.copy(resourceStream, out);
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
			final IModificationWatcher watcher = Application.get().getResourceSettings().getResourceWatcher(true);
			if (watcher != null) {
				final IChangeListener listener = new IChangeListener() {
					@Override
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

	public byte[] preProcess(ResourceSpec resourceSpec, byte[] content) {
		return _preProcessor != null ? _preProcessor.preProcess(resourceSpec, content) : content;
	}
}

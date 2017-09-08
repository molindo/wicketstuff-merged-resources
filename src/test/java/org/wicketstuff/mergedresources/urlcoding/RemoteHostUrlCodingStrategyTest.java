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

import java.net.URL;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IndexedSharedResourceCodingStrategy;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.wicketstuff.mergedresources.AbstractTestApplication;

public class RemoteHostUrlCodingStrategyTest {

	private static final ResourceReference REF = new ResourceReference(RemoteHostUrlCodingStrategyTest.class, "image");

	@Test(expected = IllegalArgumentException.class)
	public void noQuery() throws Exception {
		new RemoteHostUrlCodingStrategy(new URL("http://cdn.example.com/files?test"), "/", REF);
	}

	@Test
	public void encoding() throws Exception {
		final URL url = new URL("http://cdn.example.com/files");
		final String path = "images";

		final WicketTester tester = new WicketTester(new AbstractTestApplication() {

			@Override
			protected void mountResources() {

				mount(new RemoteHostUrlCodingStrategy(url, path, REF) {
					@Override
					protected AbstractRequestTargetUrlCodingStrategy newStrategy(final String mountPath, final String sharedResourceKey) {
						return new IndexedSharedResourceCodingStrategy(mountPath, sharedResourceKey);
					}
				});
			}
		});

		tester.startPanel(ImagePanel.class);

		tester.assertResultPage(RemoteHostResourceMountTest.class, "RemoteHostUrlCodingStrategyTest-expected-encoding.html");

	}

	public static class ImagePanel extends Panel {

		public ImagePanel(final String id) {
			super(id);
			add(new Image("img", REF, new PageParameters("0=foo/bar,1=image.png")));
		}

	}
}

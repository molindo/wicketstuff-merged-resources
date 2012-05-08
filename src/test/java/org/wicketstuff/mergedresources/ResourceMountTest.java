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

package org.wicketstuff.mergedresources;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.wicketstuff.mergedresources.ResourceMount.SuffixMismatchStrategy;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.PanelOne;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class ResourceMountTest {
	static final ResourceReference CSS_COMPONENT_B = new PackageResourceReference(ComponentB.class, "ComponentB.css");
	static final ResourceReference CSS_PANEL_ONE = new PackageResourceReference(PanelOne.class, "PanelOne.css");
	static final ResourceReference CSS_MY_FORM = new PackageResourceReference(MyForm.class, "MyForm.css");
	static final ResourceReference CSS_PRINT_COMPONENT_B = new PackageResourceReference(ComponentB.class, "ComponentB-print.css");
	static final ResourceReference CSS_PRINT_PANEL_ONE = new PackageResourceReference(PanelOne.class, "PanelOne-print.css");
	static final ResourceReference JS_COMPONENT_B = new PackageResourceReference(ComponentB.class, "ComponentB.js");
	static final ResourceReference JS_PANEL_ONE = new PackageResourceReference(PanelOne.class, "PanelOne.js");
	static final ResourceReference JS_MY_FORM = new PackageResourceReference(MyForm.class, "MyForm.js");

	private static final ResourceReference[] MERGED_CSS = {CSS_COMPONENT_B, CSS_PANEL_ONE, CSS_MY_FORM};
	private static final ResourceReference[] MERGED_CSS_PRINT = {CSS_PRINT_COMPONENT_B, CSS_PRINT_PANEL_ONE};
	private static final ResourceReference[] MERGED_JS = {JS_COMPONENT_B, JS_PANEL_ONE, JS_MY_FORM};

	/** Verify that the test page renders as expected (i.e. with each resource listed separately) during development. */
	@Test
	public void testRender_development() throws Exception {
		WicketTester tester = doRender(RuntimeConfigurationType.DEVELOPMENT);
		tester.assertResultPage(ResourceMountTest.class, "ResourceMountTestPage-development-expected.html");
	}

	/**
	 * Verify that the test page renders as expected (i.e. with merged resource href and src attributes) during
	 * deployment.
	 */
	@Test
	public void testRender_deployment() throws Exception {
		WicketTester tester = doRender(RuntimeConfigurationType.DEPLOYMENT);
		tester.assertResultPage(ResourceMountTest.class, "ResourceMountTestPage-deployment-expected.html");
	}

	/** Verify that individual, non-merged resources can be succesfully downloaded in development mode. */
	@Test
	public void testDownload_development() throws IOException {
		WicketTester tester = doRender(RuntimeConfigurationType.DEVELOPMENT);
		System.out.println(tester.getLastResponseAsString());
		assertDownloaded(tester, "static/styles.css/MyForm.css", CSS_MY_FORM);
		assertDownloaded(tester, "static/styles.css/ComponentB.css", CSS_COMPONENT_B);
		assertDownloaded(tester, "static/styles.css/PanelOne.css", CSS_PANEL_ONE);
		assertDownloaded(tester, "static/styles-print.css/ComponentB-print.css", CSS_PRINT_COMPONENT_B);
		assertDownloaded(tester, "static/styles-print.css/PanelOne-print.css", CSS_PRINT_PANEL_ONE);
		assertDownloaded(tester, "static/scripts.js/ComponentB.js", JS_COMPONENT_B);
		assertDownloaded(tester, "static/scripts.js/PanelOne.js", JS_PANEL_ONE);
	}

	/** Verify that resources are merged during deployment and can be successfully downloaded. */
	@Test
	public void testDownload_deployment() throws IOException {
		WicketTester tester = doRender(RuntimeConfigurationType.DEPLOYMENT);
		assertDownloaded(tester, "static/styles.css", MERGED_CSS);
		assertDownloaded(tester, "static/styles-print.css", MERGED_CSS_PRINT);
		assertDownloaded(tester, "static/scripts.js", MERGED_JS);
	}

	/** Verify that an exception is thrown if we execute build() without specifying a path first. */
	@Test(expected = IllegalStateException.class)
	public void testMissingPath() {
		ResourceMount b = new ResourceMount();
		b.addResourceSpec(CSS_COMPONENT_B);
		b.mount(new WicketTester().getApplication());
		fail("mount must throw exception");
	}

	/** Verify that an exception is thrown if we execute build() without specifying a resource first. */
	@Test
	public void testMissingResource() {
		ResourceMount b = new ResourceMount();
		b.setPath("/styles/all.css");
		assertNull(b.build(new WicketTester().getApplication()));
	}

	@Test
	public void testGetSuffix() {
		assertEquals("js", ResourceMount.getSuffix("/foo.js"));
		assertEquals("css", ResourceMount.getSuffix("foo.css"));
		assertNull(ResourceMount.getSuffix(".htaccess"));
		assertEquals("css", ResourceMount.getSuffix("...strange.css"));
		assertNull(ResourceMount.getSuffix("/foo.bar/baz"));
		assertNull(ResourceMount.getSuffix("/foo/."));
		assertNull(ResourceMount.getSuffix("/"));
		assertNull(ResourceMount.getSuffix("/foo"));
		assertNull(ResourceMount.getSuffix("/foo/bar"));
	}

	@Test(expected = WicketRuntimeException.class)
	public void testSuffixMismatch() {
		new ResourceMount().setPath("/foo.css").addResourceSpecs(CSS_COMPONENT_B, JS_COMPONENT_B)
				.mount(new WicketTester().getApplication());
		fail("mount must throw exception");
	}

	@Test
	public void testIgnoredSuffixMismatch() {
		new ResourceMount().setPath("/foo.css").setSuffixMismatchStrategy(SuffixMismatchStrategy.IGNORE)
				.addResourceSpecs(CSS_COMPONENT_B, JS_COMPONENT_B).mount(new WicketTester().getApplication());
	}

	@Test
	public void testNonMergedSuffixMismatch() {
		new ResourceMount().setPath("/img").setNoVersion().addResourceSpec(HomePage.class, "test.png")
				.mount(new WicketTester().getApplication());
	}

	/** Render the HomePage in either DEVELOPMENT or DEPLOYMENT mode. */
	private WicketTester doRender(final RuntimeConfigurationType mode) {
		WicketTester tester = new WicketTester(new MergedApp() {
			@Override
			public RuntimeConfigurationType getConfigurationType() {
				return mode;
			}
		});
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		return tester;
	}

	private void assertDownloaded(WicketTester tester, String uri, ResourceReference... refs) throws IOException {
		ResourceSpec[] specs = new ResourceSpec[refs.length];
		for (int i = 0; i < refs.length; i++) {
			specs[i] = new ResourceSpec(refs[i]);
		}
		assertDownloaded(tester, uri, specs);
	}

	/**
	 * Download the resource at the given URI and make sure its contents are identical to a merged list of files from the
	 * test fixture.
	 */
	private void assertDownloaded(WicketTester tester, String uri, ResourceSpec... specs) throws IOException {
		StringList expected = new StringList();
		for (ResourceSpec spec : specs) {
			InputStream is = spec.getScope().getResourceAsStream(spec.getFile());
			try {
				expected.add(IOUtils.toString(is, "UTF-8"));
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		tester.executeUrl(uri);


		// Note: merging adds two newlines between each merged file
		assertEquals(expected.join("\n\n"), tester.getLastResponse().getDocument());
	}

	/** Test app that mounts merged resources. */
	static class MergedApp extends WebApplication {
		@Override
		public Class<? extends WebPage> getHomePage() {
			return HomePage.class;
		}

		@Override
		protected void init() {
			super.init();

			getResourceSettings().setCachingStrategy(NoOpResourceCachingStrategy.INSTANCE);

			ResourceMount mount = new ResourceMount(RuntimeConfigurationType.DEVELOPMENT.equals(getConfigurationType()));
			mount.setMinifyCss(false).setMinifyJs(false);

			ResourceMount.mountWicketResourcesMerged("/wicket", this, mount);

			mount.clone().setPath("/static/styles.css").addResourceSpecs(MERGED_CSS).mount(this);
			mount.clone().setPath("/static/styles-print.css").addResourceSpecs(MERGED_CSS_PRINT).mount(this);
			mount.clone().setPath("/static/scripts.js").addResourceSpecs(MERGED_JS).mount(this);
		}
	}
}
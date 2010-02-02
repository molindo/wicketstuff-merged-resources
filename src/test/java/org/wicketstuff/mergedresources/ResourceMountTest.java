/*
 * Copyright 2010 55 Minutes (http://www.55minutes.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.mergedresources;


import java.io.InputStream;
import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.tester.WicketTester;

import org.junit.Test;
import org.wicketstuff.mergedresources.components.MyForm;
import org.wicketstuff.mergedresources.components.ComponentB;
import org.wicketstuff.mergedresources.components.PanelOne;

import static org.apache.wicket.Application.DEPLOYMENT;
import static org.apache.wicket.Application.DEVELOPMENT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


public class ResourceMountTest
{
    static final ResourceReference CSS_COMPONENT_B = new ResourceReference(
    		ComponentB.class, "ComponentB.css"
    );
    static final ResourceReference CSS_PANEL_ONE = new ResourceReference(
    		PanelOne.class, "PanelOne.css"
    );
    static final ResourceReference CSS_MY_FORM = new ResourceReference(
    		MyForm.class, "MyForm.css"
    );
    static final ResourceReference CSS_PRINT_COMPONENT_B = new ResourceReference(
    		ComponentB.class, "ComponentB-print.css"
    );
    static final ResourceReference CSS_PRINT_PANEL_ONE = new ResourceReference(
    		PanelOne.class, "PanelOne-print.css"
    );
    static final ResourceReference JS_COMPONENT_B = new ResourceReference(
    		ComponentB.class, "ComponentB.js"
    );
    static final ResourceReference JS_PANEL_ONE = new ResourceReference(
    		PanelOne.class, "PanelOne.js"
    );
    static final ResourceReference JS_MY_FORM = new ResourceReference(
    		MyForm.class, "MyForm.js"
    );
    
    private static final ResourceReference[] MERGED_CSS = {CSS_COMPONENT_B, CSS_PANEL_ONE, CSS_MY_FORM};
    private static final ResourceReference[] MERGED_CSS_PRINT = {CSS_PRINT_COMPONENT_B, CSS_PRINT_PANEL_ONE};
    private static final ResourceReference[] MERGED_JS = {JS_COMPONENT_B, JS_PANEL_ONE, JS_MY_FORM};

    
    /**
     * Verify that the test page renders as expected (i.e. with each resource
     * listed separately) during development.
     */
    @Test
    public void testRender_development() throws Exception
    {
        WicketTester tester = doRender(DEVELOPMENT);
        tester.assertResultPage(
        		ResourceMountTest.class,
            "ResourceMountTestPage-development-expected.html"
        );
    }

    /**
     * Verify that the test page renders as expected (i.e. with merged resource
     * href and src attributes) during deployment.
     */
    @Test
    public void testRender_deployment() throws Exception
    {
        WicketTester tester = doRender(DEPLOYMENT);
        tester.assertResultPage(
        		ResourceMountTest.class,
            "ResourceMountTestPage-deployment-expected.html"
        );
    }
    
    /**
     * Verify that individual, non-merged resources can be succesfully
     * downloaded in development mode.
     */
    @Test
    public void testDownload_development() throws IOException
    {
        WicketTester tester = doRender(DEVELOPMENT);
        assertDownloaded(tester, "static/styles.css/ComponentB.css", CSS_COMPONENT_B);
        assertDownloaded(tester, "static/styles.css/PanelOne.css", CSS_PANEL_ONE);
        assertDownloaded(tester, "static/styles.css/MyForm.css", CSS_MY_FORM);
        assertDownloaded(tester, "static/styles-print.css/ComponentB-print.css", CSS_PRINT_COMPONENT_B);
        assertDownloaded(tester, "static/styles-print.css/PanelOne-print.css", CSS_PRINT_PANEL_ONE);
        assertDownloaded(tester, "static/scripts.js/ComponentB.js", JS_COMPONENT_B);
        assertDownloaded(tester, "static/scripts.js/PanelOne.js", JS_PANEL_ONE);
    }

    /**
     * Verify that resources are merged during deployment and can be
     * successfully downloaded.
     */
    @Test
    public void testDownload_deployment() throws IOException
    {
        WicketTester tester = doRender(DEPLOYMENT);
        assertDownloaded(tester, "static/styles.css", MERGED_CSS);
        assertDownloaded(tester, "static/styles-print.css", MERGED_CSS_PRINT);
        assertDownloaded(tester, "static/scripts.js", MERGED_JS);
    }
    
    /**
     * Verify that an exception is thrown if we execute build() without
     * specifying a path first.
     */
    @Test(expected=WicketRuntimeException.class)
    public void testMissingPath()
    {
    	ResourceMount b = new ResourceMount();
        b.addResourceSpec(CSS_COMPONENT_B);
        b.build(new WicketTester().getApplication());
        fail("build must throw exception");
    }

    /**
     * Verify that an exception is thrown if we execute build() without
     * specifying a resource first.
     */
    @Test
    public void testMissingResource()
    {
    	ResourceMount b = new ResourceMount();
        b.setPath("/styles/all.css");
        assertNull(b.build(new WicketTester().getApplication()));
    }
    
    /**
     * Render the HomePage in either
     * DEVELOPMENT or DEPLOYMENT mode.
     */
    private WicketTester doRender(final String mode)
    {
        WicketTester tester = new WicketTester(new MergedApp() {
            @Override
            public String getConfigurationType()
            {
                return mode;
            }
        });
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
        return tester;
    }
    
    private void assertDownloaded(WicketTester tester,
            String uri,
            ResourceReference... refs) throws IOException {
    	ResourceSpec[] specs = new ResourceSpec[refs.length];
    	for (int i = 0; i < refs.length; i++) {
			specs[i] = new ResourceSpec(refs[i]);
		}
    	assertDownloaded(tester, uri, specs);
    }
    
    /**
     * Download the resource at the given URI and make sure its contents
     * are identical to a merged list of files from the test fixture.
     */
    private void assertDownloaded(WicketTester tester,
                                  String uri,
                                  ResourceSpec... specs)
        throws IOException
    {
        StringList expected = new StringList();
        for(ResourceSpec spec : specs)
        {
            InputStream is = spec.getScope().getResourceAsStream(spec.getFile());
            try
            {
                expected.add(IOUtils.toString(is, "UTF-8"));
            }
            finally
            {
                IOUtils.closeQuietly(is);
            }
        }
        WebRequestCycle wrc = tester.setupRequestAndResponse(false);
        tester.getServletRequest().setURL(uri);
        tester.processRequestCycle(wrc);
        
        // Note: merging adds two newlines between each merged file
        assertEquals(
            expected.join("\n\n"),
            tester.getServletResponse().getDocument()
        );
    }

    /**
     * Test app that mounts merged resources.
     */
    static class MergedApp extends WebApplication
    {
		@Override
        public Class<? extends WebPage> getHomePage()
        {
            return HomePage.class;
        }

        @Override
        protected void init()
        {
            super.init();
            
            ResourceMount mount = new ResourceMount(Application.DEVELOPMENT.equals(getConfigurationType()));
            mount.setMinifyCss(false).setMinifyJs(false);
            
            ResourceMount.mountWicketResources("/wicket", this, mount);
            
            mount.clone().setPath("/static/styles.css")
                                       .addResourceSpecs(MERGED_CSS)
                                       .mount(this);
            mount.clone().setPath("/static/styles-print.css")
                                       .addResourceSpecs(MERGED_CSS_PRINT)
                                       .mount(this);
            mount.clone().setPath("/static/scripts.js")
                                       .addResourceSpecs(MERGED_JS)
                                       .mount(this);
        }
    }
}
package org.wicketstuff.mergedresources.components;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.wicketstuff.mergedresources.AnnotationTestApplication;
import org.wicketstuff.mergedresources.annotations.CssContribution;
import org.wicketstuff.mergedresources.annotations.JsContribution;

@JsContribution(value = "PanelOne.js", path = AnnotationTestApplication.ALL_JS)
@CssContribution(value = "PanelOne.css", path = AnnotationTestApplication.ALL_CSS)
public class PanelOne extends Panel {

	private static final long serialVersionUID = 1L;

	public PanelOne(String id) {
		super(id);
		add(CSSPackageResource.getHeaderContribution(PanelOne.class, PanelOne.class.getSimpleName() + ".css"));
		add(JavascriptPackageResource.getHeaderContribution(PanelOne.class, PanelOne.class.getSimpleName() + ".js"));
		add(new MyForm("myForm"));
		add(new ComponentB("componentB"));
	}

}

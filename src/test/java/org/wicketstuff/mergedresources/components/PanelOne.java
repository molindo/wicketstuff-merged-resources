package org.wicketstuff.mergedresources.components;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.panel.Panel;

public class PanelOne extends Panel {

	private static final long serialVersionUID = 1L;

	public PanelOne(String id) {
		super(id);
		add(CSSPackageResource.getHeaderContribution(PanelOne.class, PanelOne.class.getSimpleName() + ".css"));
		add(CSSPackageResource.getHeaderContribution(PanelOne.class, PanelOne.class.getSimpleName() + "-print.css", "print"));
		add(JavascriptPackageResource.getHeaderContribution(PanelOne.class, PanelOne.class.getSimpleName() + ".js"));
		add(new MyForm("myForm"));
		add(new ComponentB("componentB"));
	}

}

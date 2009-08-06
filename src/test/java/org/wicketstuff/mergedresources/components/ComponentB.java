package org.wicketstuff.mergedresources.components;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class ComponentB extends Panel {

	private static final long serialVersionUID = 1L;

	public ComponentB(String id) {
		super(id);
		add(CSSPackageResource.getHeaderContribution(ComponentB.class, ComponentB.class.getSimpleName() + ".css"));
		add(JavascriptPackageResource.getHeaderContribution(ComponentB.class, ComponentB.class.getSimpleName() + ".js"));
		add(new Label("label", "Wicket!"));
	}

}

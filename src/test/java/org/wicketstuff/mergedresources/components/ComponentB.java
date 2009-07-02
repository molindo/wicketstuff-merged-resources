package org.wicketstuff.mergedresources.components;

import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class ComponentB extends Panel {

	private static final long serialVersionUID = 1L;

	public ComponentB(String id) {
		super(id);
		add(HeaderContributor.forCss(ComponentB.class, ComponentB.class.getSimpleName() + ".css"));
		add(HeaderContributor.forJavaScript(ComponentB.class, ComponentB.class.getSimpleName() + ".js"));
		add(new Label("label", "Wicket!"));
	}

}

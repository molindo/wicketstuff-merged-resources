package org.wicketstuff.mergedresources.annotations.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.wicketstuff.mergedresources.annotations.CssContribution;
import org.wicketstuff.mergedresources.annotations.JsContribution;

@JsContribution
@CssContribution
public class ComponentB extends Panel {

	private static final long serialVersionUID = 1L;

	public ComponentB(String id) {
		super(id);
		add(new Label("label", "Wicket!"));
	}

}

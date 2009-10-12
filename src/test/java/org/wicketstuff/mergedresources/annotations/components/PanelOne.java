package org.wicketstuff.mergedresources.annotations.components;

import org.apache.wicket.markup.html.panel.Panel;
import org.wicketstuff.mergedresources.annotations.CssContribution;
import org.wicketstuff.mergedresources.annotations.CssContributions;
import org.wicketstuff.mergedresources.annotations.JsContribution;
import org.wicketstuff.mergedresources.annotations.ResourceContribution;

@JsContribution({"", "functions.js"})
@CssContributions({@CssContribution, @CssContribution(media = "print")})
@ResourceContribution(value = "accept.png", path = "/img/accept.png")
public class PanelOne extends Panel {

	private static final long serialVersionUID = 1L;

	public PanelOne(String id) {
		super(id);
		add(new MyForm("myForm"));
		add(new ComponentB("componentB"));
	}

}

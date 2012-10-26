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

package org.wicketstuff.mergedresources.components;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ComponentB extends Panel {

	private static final long serialVersionUID = 1L;

	public ComponentB(String id) {
		super(id);
		add(new Label("label", "Wicket!"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(new CssResourceReference(ComponentB.class, ComponentB.class.getSimpleName() + ".css")));
		response.render(CssHeaderItem.forReference(new CssResourceReference(ComponentB.class, ComponentB.class.getSimpleName() + "-print.css"), "print"));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(ComponentB.class, ComponentB.class.getSimpleName() + ".js")));
		super.renderHead(response);
	}
}

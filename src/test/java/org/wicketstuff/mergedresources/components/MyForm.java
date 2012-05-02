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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class MyForm extends Form<Object> {

	private static final long serialVersionUID = 1L;

	public MyForm(String id) {
		super(id);
		add(new AttributeAppender("onsubmit", true, new Model<String>("return validateMyForm()"), ";"));
		add(new AjaxFormSubmitBehavior(this, "onclick") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onError(AjaxRequestTarget target) {
				// nothing

			}

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				// nothing
			}

		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(new CssResourceReference(MyForm.class, MyForm.class.getSimpleName() + ".css"));
		response.renderJavaScriptReference(new JavaScriptResourceReference(MyForm.class, MyForm.class.getSimpleName() + ".js"));
		super.renderHead(response);
	}
}

package org.wicketstuff.mergedresources.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

public class MyForm extends Form {

	private static final long serialVersionUID = 1L;

	public MyForm(String id) {
		super(id);
		add(HeaderContributor.forCss(MyForm.class, MyForm.class.getSimpleName() + ".css"));
		add(HeaderContributor.forJavaScript(MyForm.class, MyForm.class.getSimpleName() + ".js"));
		add(new AttributeAppender("onsubmit", true, new Model("return validateMyForm()"), ";"));
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

}

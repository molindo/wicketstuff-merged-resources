package org.wicketstuff.mergedresources.annotations;

import java.util.HashMap;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.application.IComponentInstantiationListener;

public class ContributionInjector implements IComponentInstantiationListener {

	private HashMap<Class<? extends Component>, HeaderContribution> _contributions = new HashMap<Class<? extends Component>, HeaderContribution>();
	
	public void onInstantiation(Component component) {
		if (eligible(component)) {
			asMarkupContainer(component).add(getHeaderContribution(component.getClass()));
		}
	}

	private HeaderContribution getHeaderContribution(Class<? extends Component> cls) {
		HeaderContribution hc = _contributions.get(cls);
		if (hc == null) {
			synchronized (_contributions) {
				hc = new HeaderContribution(cls);
				_contributions.put(cls, hc);
			}
		}
		return hc;
	}

	private MarkupContainer asMarkupContainer(Component component) {
		return (MarkupContainer) component;
	}

	private boolean hasResourceAnnotation(Component component) {
		return component.getClass().getAnnotation(JsContribution.class) != null || component.getClass().getAnnotation(CssContribution.class) != null;
	}

	private boolean eligible(Component component) {
		return MarkupContainer.class.isAssignableFrom(component.getClass()) && hasResourceAnnotation(component);
	}
	
}
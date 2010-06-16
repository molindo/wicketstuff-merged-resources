package org.wicketstuff.mergedresources.annotations;

import java.util.HashMap;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.application.IComponentInstantiationListener;

public class ContributionInjector implements IComponentInstantiationListener {

	private HashMap<Class<? extends Component>, HeaderContribution> _contributions = new HashMap<Class<? extends Component>, HeaderContribution>();
	private ContributionScanner contributionScanner;
	
	public ContributionInjector(ContributionScanner contributionScanner) {
		this.contributionScanner = contributionScanner;
	}

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

	private boolean eligible(Component component) {
		return contributionScanner.hasContribution(component);
	}
	
}
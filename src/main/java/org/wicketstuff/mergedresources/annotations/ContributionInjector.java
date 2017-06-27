/**
 * Copyright 2016 Molindo GmbH
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
package org.wicketstuff.mergedresources.annotations;

import java.util.HashMap;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.application.IComponentInstantiationListener;

public class ContributionInjector implements IComponentInstantiationListener {

	private final HashMap<Class<? extends Component>, HeaderContribution> _contributions = new HashMap<>();

	@Override
	public void onInstantiation(final Component component) {
		if (eligible(component)) {
			asMarkupContainer(component).add(getHeaderContribution(component.getClass()));
		}
	}

	private HeaderContribution getHeaderContribution(final Class<? extends Component> cls) {
		HeaderContribution hc = _contributions.get(cls);
		if (hc == null) {
			synchronized (_contributions) {
				hc = new HeaderContribution(cls);
				_contributions.put(cls, hc);
			}
		}
		return hc;
	}

	private MarkupContainer asMarkupContainer(final Component component) {
		return (MarkupContainer) component;
	}

	private boolean hasResourceAnnotation(final Component component) {
		return component.getClass().getAnnotation(JsContribution.class) != null
				|| component.getClass().getAnnotation(CssContribution.class) != null;
	}

	private boolean eligible(final Component component) {
		return MarkupContainer.class.isAssignableFrom(component.getClass()) && hasResourceAnnotation(component);
	}

}

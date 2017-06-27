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
package org.wicketstuff.mergedresources.versioning;

public final class SimpleResourceVersion extends AbstractResourceVersion {

	private static final long serialVersionUID = 1L;
	private int _value;

	public SimpleResourceVersion(final int value) {
		setValue(value);
	}

	public int getValue() {
		return _value;
	}

	private void setValue(final int value) {
		if (value < 0) {
			throw new IllegalArgumentException("value must be > 0 (valid) or 0 (invalid)");
		}
		_value = value;
	}

	@Override
	public boolean isValid() {
		return _value > 0;
	}

	@Override
	public String getVersion() {
		return Integer.toString(_value);
	}

	@Override
	protected int compareValid(final AbstractResourceVersion o) throws IncompatibleVersionsException {
		if (o instanceof SimpleResourceVersion) {
			return ((Integer) getValue()).compareTo(((SimpleResourceVersion) o).getValue());
		} else {
			throw new IncompatibleVersionsException(this, o);
		}
	}

}

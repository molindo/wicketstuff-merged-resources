package org.wicketstuff.mergedresources.versioning;


public class StaticResourceVersionProvider implements IResourceVersionProvider {

	private int _value;

	public StaticResourceVersionProvider(int value) {
		// validate value
		new SimpleResourceVersion(_value);
		_value = value;
	}
	
	public AbstractResourceVersion getVersion(Class<?> scope, String file)
			throws VersionException {
		return new SimpleResourceVersion(_value);
	}

}

package org.wicketstuff.mergedresources.preprocess;

public abstract class StringResourcePreProcessor implements IResourcePreProcessor {

	public byte[] preProcess(byte[] content) {
		return preProcess(new String(content)).getBytes();
	}

	protected abstract String preProcess(String string);

}

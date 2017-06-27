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
package org.wicketstuff.mergedresources.preprocess;

import java.io.Serializable;

import javax.annotation.CheckForNull;

import org.wicketstuff.mergedresources.ResourceSpec;

public interface IResourcePreProcessor extends Serializable {

	/**
	 * 
	 * @param resourceSpec
	 *            null when processing complete merged resource, non-null for
	 *            each part of a merged resource
	 * @param content
	 * 
	 * @return processed content, may be <code>content</code>
	 */
	byte[] preProcess(@CheckForNull ResourceSpec resourceSpec, byte[] content);

}
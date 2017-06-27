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
package org.wicketstuff.mergedresources.preprocess.css;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wicketstuff.mergedresources.ResourceSpec;
import org.wicketstuff.mergedresources.preprocess.StringResourcePreProcessor;

/**
 * <p> Replaces relative paths in CSS files to full path urls. </p>
 * 
 * <p> When you add multiple CSS files of which the source location differs, the
 * path of the CSS file will refer to the wrong background images (for example).
 * This preprocessor replaces the relative paths in CSS files by their
 * equivalent absolute url. </p>
 * 
 * <p> See the CSS url spec at http://www.w3.org/TR/CSS21/syndata.html#uri </p>
 * 
 * <blockquote> The format of a URI value is 'url(' followed by optional white
 * space followed by an optional single quote (') or double quote (
 * ") character followed by the URI itself, followed by an optional single quote (') or double quote ("
 * ) character followed by optional white space followed by ')'. The two quote
 * characters must be the same. </blockquote>
 */
public class CssUrlRewritingResourcePreProcessor extends StringResourcePreProcessor {
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String preProcess(ResourceSpec resourceSpec, String string) {
		// Only preprocess partial files with CSS extension
		if (resourceSpec == null || !resourceSpec.getFile().toLowerCase().endsWith(".css")) {
			return string;
		}

		StringBuffer processedString = new StringBuffer();

		// Matches urls according to the CSS url specification quoted in javadoc
		// above.
		Pattern pattern = Pattern.compile("(url\\s*\\(\\s*\\\"?\'?)([^\\\\/].*)(\\s*\\\"?\\'?\\))");

		Matcher urlMatcher = pattern.matcher(string);

		while (urlMatcher.find()) {
			// Group 1 is the part "url(", starting the url
			String start = urlMatcher.group(1);

			// Group 2 is the path, e.g. "test.png"
			String path = urlMatcher.group(2);

			// Group 3 is the part ")", ending the url
			String end = urlMatcher.group(3);

			String replacementString = getFullPath(resourceSpec, path);

			urlMatcher.appendReplacement(processedString, Matcher.quoteReplacement(start + replacementString + end));
		}

		urlMatcher.appendTail(processedString);

		return processedString.toString();
	}

	/**
	 * Get the full path for the given CSS url(). This will replace relative
	 * paths with absolute paths. The {@link ResourceSpec} is used to get the
	 * full path.
	 * 
	 * Cannot use Wicket 'urlFor' method, because there is no requestcycle here.
	 * Plus it would mount the given resourceReference in the application
	 * 
	 * @param resourceSpec
	 *            the {@link ResourceSpec} used to get the full path.
	 * @param path
	 *            the old (relative) path.
	 * @return the full path for the resource, Wicket specific.
	 */
	private String getFullPath(ResourceSpec resourceSpec, String path) {
		StringBuilder sb = new StringBuilder();

		// Append "/resources/"
		sb.append(File.separator).append("resources").append(File.separator);

		// This will return the full class name of the Scope class (for example
		// the Panel this
		// ResourceReference is bound to). In Wicket this is part of the path.
		sb.append(resourceSpec.getScope().getName());

		sb.append(File.separator);

		// If the CSS file is in "res/styling.css", this will return "res/"
		sb.append(getPath(resourceSpec.getFile()));

		// The path of the relative url, for example "test.png"
		sb.append(path);

		return sb.toString();
	}

	/**
	 * Get the path for the given file location string.
	 * 
	 * @param file
	 *            the file to get the directory path for
	 * @return the path.
	 */
	private CharSequence getPath(String filePath) {
		File file = new File(filePath);
		String path = file.getPath();

		return path.subSequence(0, Long.valueOf(path.length() - file.getName().length()).intValue());
	}
}

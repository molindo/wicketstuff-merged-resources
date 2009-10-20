/**
 * 
 */
package org.wicketstuff.mergedresources.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author stf
 *
 * Use CssMultiContribution if you need to add CssContributions for different
 * media types for instance
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CssContributions {
	CssContribution[] value();
}
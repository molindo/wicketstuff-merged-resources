/**
 * 
 */
package org.wicketstuff.mergedresources.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.wicketstuff.mergedresources.ResourceMount;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsContribution {
	
	/**
	 * file names relative to annotated type. type.getClass().getSimpleName() + ".js" if emtpy
	 */
	String[] value() default "";
	
	/**
	 * the mount path: if it starts with '/' it's treated as an absolute path, 
	 * relative to {@link ResourceMount#setPath(String)} otherwise. 
	 */
	String path() default "";
	
	/**
	 * merged resources are sorted by their order value, starting with the highest,
	 * ending with the lowest
	 */
	int order() default 0;
}
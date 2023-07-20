package tfc.hookin.annotation.hooks;

import tfc.hookin.annotation.params.MethodTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MethodRedir {
	//@formatter:off
	/**
	 * the method to target
	 */
	MethodTarget target();
	
	/**
	 * format:
	 * [methodName] (TODO)
	 * only works for non-static methods
	 * automatically picks descriptor and class based off method arguments
	 *
	 * L[owner/ClassName];[methodName]
	 * automatically picks descriptor based off method arguments
	 *
	 * L[owner/ClassName];[methodName]([descArgs])[descReturnType]
	 */
	String[] redirect();
	MethodTarget[] exclude() default {};
	//@formatter:on
}

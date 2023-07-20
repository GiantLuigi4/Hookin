package tfc.hookin.annotation.hooks;


import tfc.hookin.annotation.params.MethodTarget;
import tfc.hookin.annotation.params.Point;
import tfc.hookin.struct.template.params.MethodTargetStruct;
import tfc.hookin.struct.template.params.PointStruct;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface Inject {
	MethodTarget[] target();
	Point point();
	
	/**
	 * Marks the callback as cancelable
	 * This will impact performance
	 */
	boolean cancellable() default false;
}

package tfc.hookin.annotation.hooks.dangerous;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * swaps the super class of the target class
 * useful for extending final classes that ATs don't work on
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface SwapSuper {
	Class<?> value();
}

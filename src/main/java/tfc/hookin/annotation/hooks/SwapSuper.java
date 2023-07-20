package tfc.hookin.annotation.hooks;

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
@Deprecated // hints IDE to warn devs who use this
public @interface SwapSuper {
	Class<?> value();
}

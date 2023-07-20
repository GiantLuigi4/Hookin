package tfc.hookin.annotation.hinting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@formatter:off
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ShadowOverride {}

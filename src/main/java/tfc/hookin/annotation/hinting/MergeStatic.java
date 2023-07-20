package tfc.hookin.annotation.hinting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * tells hookin to merge a static field into the target class instead of leaving it in the hook class
 * however, static methods are automatically merged by default, so this is unnecessary for them
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface MergeStatic {
}

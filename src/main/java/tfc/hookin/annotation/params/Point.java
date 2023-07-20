package tfc.hookin.annotation.params;

public @interface Point {
	Type value();
	MethodTarget methodTarget() default @MethodTarget("");
	Shift shift() default Shift.AFTER;
	
	enum Type {
		HEAD,
		FIELD_GET,
		FIELD_SET,
		INVOCATION,
		FINAL_RETURN,
		RETURN
	}
	
	enum Shift {
		/**
		 * before parameter calculations
		 * only applicable for invocation
		 */
		START,
		/**
		 * directly before the instruction
		 */
		BEFORE,
		/**
		 * directly after the instruction
		 */
		AFTER
	}
}

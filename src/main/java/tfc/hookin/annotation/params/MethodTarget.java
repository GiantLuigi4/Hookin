package tfc.hookin.annotation.params;

public @interface MethodTarget {
	String[] value();
	
	boolean regex() default false;
	
	boolean mappable() default true;
	
	boolean includeDesc() default false;
	
	AnnotationTemplate[] annotations() default {};
	
	boolean matchAllAnnotations() default true;
}

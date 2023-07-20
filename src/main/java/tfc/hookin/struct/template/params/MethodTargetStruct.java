package tfc.hookin.struct.template.params;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.hookin.internal.annotation.HookinMerged;
import tfc.hookin.platform.PlatformCalls;
import tfc.hookin.struct.BaseStruct;
import tfc.hookin.util.AnnotationParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MethodTargetStruct extends BaseStruct {
	String[] value;
	boolean regex;
	boolean mappable;
	boolean includeDesc;
	AnnotationTemplateStruct[] annotations;
	boolean matchAllAnnotations;
	
	public MethodTargetStruct(String[] value, boolean regex, boolean mappable, boolean includeDesc, AnnotationTemplateStruct[] annotations, boolean matchAllAnnotations) {
		this.value = value;
		this.regex = regex;
		this.mappable = mappable;
		this.includeDesc = includeDesc;
		this.annotations = annotations;
		this.matchAllAnnotations = matchAllAnnotations;
	}
	
	public MethodTargetStruct() {
	}
	
	@Override
	public boolean handleNull(String key) {
		switch (key) {
			case "regex":
				regex = false;
				return true;
			case "annotations":
				annotations = new AnnotationTemplateStruct[0];
				return true;
			case "includeDesc":
				includeDesc = false;
				return true;
			case "matchAllAnnotations":
				matchAllAnnotations = true;
				return true;
			case "mappable":
				mappable = !regex;
				return true;
		}
		return false;
	}
	
	public boolean annotationMatches(List<AnnotationNode> annotationNodes) {
		if (annotations.length == 0) return true;
		
		if (matchAllAnnotations) {
			for (AnnotationTemplateStruct annotation : annotations) {
				boolean matchedAny = false;
				for (AnnotationNode allAnnotation : annotationNodes) {
					if (annotation.matches(allAnnotation))
						matchedAny = true;
				}
				if (!matchedAny)
					return false;
			}
			return true;
		} else {
			for (AnnotationTemplateStruct annotation : annotations) {
				for (AnnotationNode allAnnotation : annotationNodes) {
					if (annotation.matches(allAnnotation)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	protected String map$(boolean includeClass, String target) {
		String clazz, methodName, desc;
		
		if (target.contains(";") && target.indexOf(";") < target.indexOf("(")) {
			String[] split = target.split(";", 2);
			clazz = split[0] + ";";
			target = split[1];
		} else clazz = null;
		
		if (target.contains("(")) {
			String[] split = target.split("\\(", 2);
			methodName = split[0];
			desc = "(" + split[1];
		} else {
			methodName = target;
			desc = null;
		}
		
		methodName = PlatformCalls.mapMethod(clazz, methodName, desc);
		
		if (desc != null) desc = PlatformCalls.mapDesc(desc);
		else desc = "";
		
		if (clazz != null) clazz = "L" + PlatformCalls.mapClass(clazz.substring(1, clazz.length() - 1)) + ";";
		else clazz = "";
		
		if (includeClass)
			return clazz + methodName + desc;
		else return methodName + desc;
	}
	
	protected String map(boolean includeClass, String fallbackDesc, String target) {
		if (target.contains(";") || !PlatformCalls.requiresClassName()) {
			if (target.contains("(")) {
				return map$(includeClass, target);
			} else {
				if (fallbackDesc != null) {
					return map$(includeClass, target + fallbackDesc);
				} else if (!PlatformCalls.requiresDescriptor()) {
					return map$(includeClass, target);
				}
			}
		}
		return target;
	}
	
	public boolean matches(String fallbackDesc, MethodNode method) {
		return matches(fallbackDesc, method.name, method.desc, AnnotationParser.allAnnotations(method));
	}
	
	public boolean matches(String fallbackDesc, String name, String desc) {
		return matches(fallbackDesc, name, desc, new ArrayList<>());
	}
	
	public boolean matches(String fallbackDesc, String name, String desc, List<AnnotationNode> annotationNodes) {
		if (mappable && regex) throw new RuntimeException("Regex filters cannot be mapped");
		
		for (AnnotationNode allAnnotation : annotationNodes) {
			if (AnnotationParser.isAnnotation(HookinMerged.class, allAnnotation)) {
				for (String s : value) {
					if (name.equals(s)) {
						return true;
					}
				}
				return false;
			}
		}

//		String testName = includeDesc ? method.name + method.desc : method.name;
		String testName = name + desc;
		
		if (regex) {
			for (String s : value) {
				Predicate<String> pattern = Pattern.compile("^" + s + "$").asPredicate(); // asMatchPredicate
				if (pattern.test(testName))
					return annotationMatches(annotationNodes);
			}
		} else {
			for (String s : value) {
				if (testName.equals(map(false, fallbackDesc, s)))
					return annotationMatches(annotationNodes);
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder text;
		if (value.length == 1) text = new StringBuilder(value[0]);
		else {
			text = new StringBuilder("[");
			for (int i = 0; i < value.length - 1; i++) text.append(value[i]).append(", ");
			text.append(value[value.length - 1]).append("]");
		}
		String prefix = regex ? "regex:" : (mappable ? "mapped:" : "verbatim:");
		
		String annotationStr;
		if (annotations.length == 0) annotationStr = "";
		else annotationStr = " annotations:" + annotations.length + (matchAllAnnotations ? "exact" : "loose");
		return prefix + text + annotationStr;
	}
}

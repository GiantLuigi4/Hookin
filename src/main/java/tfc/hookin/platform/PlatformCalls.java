package tfc.hookin.platform;

import tfc.hookin.util.TriFunction;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class PlatformCalls {
	private static final PlatformCalls calls = new PlatformCalls();
	
	/**
	 * whether or not a class name must be specified in order for a method/field to be remappable
	 */
	public boolean requireClassName = false;
	/**
	 * whether or not a descriptor must be specified in order for a method/field to be remappable
	 */
	public boolean requireDescriptor = false;
	
	public Function<String, String> classMapper = (name) -> name;
	public TriFunction<String, String, String, String> methodMapper = (className, methodName, descriptor) -> methodName;
	public TriFunction<String, String, String, String> fieldMapper = (className, fieldName, descriptor) -> fieldName;
	public Function<String, String> descriptorMapper = (name) -> {
		// TODO: default impl which iterates over the arguments
		return name;
	};
	
	/**
	 * int level
	 * the level to log the text at
	 * 0=info, 1=warn, 2=error, 3=fatal
	 * String text
	 * the text to log
	 */
	public BiConsumer<Integer, String> logger = (level, text) -> {
		if (level < 2) System.out.println(text);
		else System.err.println(text);
	};
	
	public static void log(String s) {
		calls.logger.accept(0, s);
	}
	
	public static void warn(String s) {
		calls.logger.accept(1, s);
	}
	
	public static void err(String s) {
		calls.logger.accept(2, s);
	}
	
	public static void fatal(String s) {
		calls.logger.accept(3, s);
	}
	
	public static String mapClass(String substring) {
		return calls.classMapper.apply(substring);
	}
	
	public static String mapDesc(String desc) {
		return calls.descriptorMapper.apply(desc);
	}
	
	public static String mapMethod(String clazz, String methodName, String desc) {
		return methodName;
	}
	
	public static boolean requiresClassName() {
		return calls.requireClassName;
	}
	
	public static boolean requiresDescriptor() {
		return calls.requireDescriptor;
	}
	
	private static boolean ranSetup = false;
	
	public static void runSetup(Setup setup) {
		if (ranSetup) throw new RuntimeException();
		setup.run(calls);
		ranSetup = true;
	}
	
	public static boolean isDev() {
		return true;
	}
}

package test;

public class HookTarget {
	public static void main(String[] args) {
		printLn("Test");
		
		System.out.println(returnableMethod());
		
		System.out.println(returnableMethod2());
		
		System.out.println(aMethodThatDoesImportantCalculations());
	}
	
	public static float returnableMethod2() {
		System.out.println("Hello");
		System.out.println(returnableMethod());
		
		return 42f;
	}
	
	public static Object returnableMethod() {
		return new Object();
	}
	
	public static void printLn(String text) {
		System.err.println(text);
	}
	
	public static int aMethodThatDoesImportantCalculations() {
		int ft = 42;
		int fs = 56;
		
		return ft + fs;
	}
}

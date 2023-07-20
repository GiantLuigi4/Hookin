package tfc.hookin.internal;

public class DescriptorUtils {
	public static String getObjName(char type) {
		switch (type) {
			//@formatter:off
			case 'B': return "java/lang/Byte";
			case 'C': return "java/lang/Character";
			case 'D': return "java/lang/Double";
			case 'F': return "java/lang/Float";
			case 'I': return "java/lang/Integer";
			case 'J': return "java/lang/Long";
			case 'S': return "java/lang/Short";
			case 'Z': return "java/lang/Boolean";
			//@formatter:on
		}
		return null;
	}
	
	public static char getPrimitiveSymbol(String type) {
		switch (type) {
			//@formatter:off
			case "java/lang/Byte": return 'B';
			case "java/lang/Character": return 'C';
			case "java/lang/Double": return 'D';
			case "java/lang/Float": return 'F';
			case "java/lang/Integer": return 'I';
			case "java/lang/Long": return 'J';
			case "java/lang/Short": return 'S';
			case "java/lang/Boolean": return 'Z';
			//@formatter:on
		}
		return 'L';
	}
	
	public static String getPrimitiveName(char symbol) {
		switch (symbol) {
			//@formatter:off
			case 'B': return "byte";
			case 'C': return "char";
			case 'D': return "double";
			case 'F': return "float";
			case 'I': return "int";
			case 'J': return "long";
			case 'S': return "short";
			case 'Z': return "boolean";
			case 'L': return "object";
			case 'A': return "[]";
			//@formatter:on
		}
		return null;
	}
	
	public static int countArgs(String desc) {
		int i = 0;
		
		desc = desc.substring(1, desc.lastIndexOf(')'));
		
		while (!desc.isEmpty()) {
			while (desc.charAt(0) == '[') desc = desc.substring(1);
			
			if (desc.charAt(0) == 'L')
				desc = desc.substring(desc.indexOf(';'));
			desc = desc.substring(1);
			
			i++;
		}
		
		return i;
	}
	
	public static String extractReturnType(String desc) {
		return desc.substring(desc.indexOf(")") + 1);
	}
}

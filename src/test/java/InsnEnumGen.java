import org.objectweb.asm.Opcodes;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;

// https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions
public class InsnEnumGen {
	private static boolean checkOp(String name) {
		try {
			Opcodes.class.getDeclaredField(name);
			return true;
		} catch (Throwable ignored) {
		}
		return false;
	}
	
	private static int getOp(String s) {
		try {
			Field f = Opcodes.class.getDeclaredField(s);
			f.setAccessible(true);
			return (Integer) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	protected static void addEnum(StringBuilder text, String name, int add, int sub) {
		addEnum(text, name, add, sub, false);
	}
	
	protected static void maybeAddEnum(StringBuilder text, String name, int add, int sub) {
		maybeAddEnum(text, name, add, sub, false);
	}
	
	protected static void addEnum(StringBuilder text, String name, int add, int sub, boolean clear) {
		text.append(name).append("(").append(getOp(name)).append(", ").append(add).append(", ").append(sub).append("),\n");
	}
	
	protected static void maybeAddEnum(StringBuilder text, String name, int add, int sub, boolean clear) {
		if (checkOp(name))
			text.append(name).append("(").append(getOp(name)).append(", ").append(add).append(", ").append(sub).append("),\n");
	}

//	protected static int getSize(char c) {
//		return (c == 'D' || c == 'F') ? 2 : 1;
//	}
	
	protected static String map(char c) {
		switch (c) {
			case 'I':
				return "int";
			case 'L':
				return "long";
			case 'F':
				return "float";
			case 'D':
				return "double";
			default:
				throw new RuntimeException();
		}
	}
	
	public static void main(String[] args) {
		StringBuilder text = new StringBuilder();
		
		String types = "ILFD";
		String allTypes = "ABCDFILS";
		String[] ops = new String[]{
				"DIV", "MUL",
				"ADD", "SUB",
				"REM",
				"SHL", "SHR", // shift left, shift right
				"XOR", "OR",
				"USHR", // bitwise shift right
				"AND",
				"CMPG", "CMPL", // compare greater, compare less
				"CMP", // compare
				"NEG", // not
		};
		
		for (char c : types.toCharArray()) {
			text.append("// ").append(map(c)).append(" math\n");
			for (String op : ops) {
				if (op.equals("XOR")) {
					text.append("// ").append(map(c)).append(" logic\n");
				}
				
				maybeAddEnum(text, c + op, 1, 2);
			}
		}
		
		text.append("// objects\n");
		addEnum(text, "NEW", 1, 0);
		addEnum(text, "MONITORENTER", 1, 0);
		addEnum(text, "MONITOREXIT", 1, 0);
		addEnum(text, "ATHROW", 1, 1);
		addEnum(text, "RETURN", 0, 0, true);
		for (char c : allTypes.toCharArray())
			maybeAddEnum(text, c + "RETURN", 0, 1, true);
		
		text.append("// stack manipulation\n");
		addEnum(text, "DUP", 2, 1);
		addEnum(text, "POP", 0, 1);
		// TODO: deal with POP2
		// TODO: single elements for SWAP, not words
		addEnum(text, "SWAP", 2, 2);
		for (char c : allTypes.toCharArray()) maybeAddEnum(text, c + "IPUSH", 1, 0);
		
		text.append("// jumps\n");
		String[] values = new String[]{
				"IFEQ", "IFNE", // ==, !=
				"IFGE", "IFGT", // >=, >
				"IFLE", "IFLT", // <=, <
				"IFNONNULL", // != null
				"IFNULL", // == null
		};
		for (String value : values) addEnum(text, value, 0, 1);
		
		values = new String[]{
				"IF_ICMPEQ",
				"IF_ICMPNE",
				
				"IF_ICMPGE",
				"IF_ICMPGT",
				
				"IF_ICMPLE",
				"IF_ICMPLT",
				
				"IF_ACMPEQ",
				"IF_ACMPNE",
		};
		for (String value : values) addEnum(text, value, 0, 2);
		addEnum(text, "GOTO", 0, 0);
		
		text.append("// switches\n");
		addEnum(text, "TABLESWITCH", 0, 1);
		addEnum(text, "LOOKUPSWITCH", 0, 1);
		
		text.append("// load local\n");
		for (char c : allTypes.toCharArray()) {
			maybeAddEnum(text, c + "LOAD", 1, 0);
			for (int i = 0; i < 5; i++) maybeAddEnum(text, c + "LOAD_" + i, 1, 0);
		}
		
		text.append("// set local\n");
		for (char c : allTypes.toCharArray()) {
			maybeAddEnum(text, c + "STORE", 0, 1);
			for (int i = 0; i < 5; i++) maybeAddEnum(text, c + "STORE_" + i, 0, 1);
		}
		
		text.append("// array manipulation\n");
		addEnum(text, "ANEWARRAY", 1, 1);
		addEnum(text, "NEWARRAY", 1, 1);
		addEnum(text, "ARRAYLENGTH", 1, 1);
		
		text.append("// set array element\n");
		for (char c : allTypes.toCharArray())
			addEnum(text, c + "ASTORE", 0, 3);
		
		text.append("// load array element\n");
		for (char c : allTypes.toCharArray()) addEnum(text, c + "ALOAD", 1, 2);
		
		text.append("// constants\n");
		addEnum(text, "ACONST_NULL", 1, 0);
		for (char c : allTypes.toCharArray()) {
			for (int i = 0; i <= 5; i++) {
				maybeAddEnum(text, c + "CONST_" + i, 1, 0);
				maybeAddEnum(text, c + "CONST_M" + i, 1, 0);
			}
		}
		
		text.append("// casts\n");
		for (char c0 : allTypes.toCharArray()) {
			for (char c1 : allTypes.toCharArray()) {
				maybeAddEnum(text, c0 + "2" + c1, 1, 1);
			}
		}
		
		System.out.println(text);
		
		// https://stackoverflow.com/a/6713290
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.toString().trim()), null);
	}
}

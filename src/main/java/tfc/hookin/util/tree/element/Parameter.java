package tfc.hookin.util.tree.element;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Parameter {
	public final String type;
	public final AbstractInsnNode start;
	public final AbstractInsnNode end;
	
	public Parameter(String type, AbstractInsnNode start, AbstractInsnNode end) {
		this.type = type;
		this.start = start;
		this.end = end;
	}
}

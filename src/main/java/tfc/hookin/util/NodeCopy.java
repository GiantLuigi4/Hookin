package tfc.hookin.util;

import org.objectweb.asm.tree.*;

public class NodeCopy {
	public static MethodNode copy(MethodNode oldMethod, String oldOwner, String newOwner) {
		MethodNode newNode = new MethodNode();
		oldMethod.accept(newNode);
		
		for (AbstractInsnNode instruction : newNode.instructions) {
			if (instruction instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) instruction;
				if (methodNode.owner.equals(oldOwner))
					methodNode.owner = newOwner;
			} else if (instruction instanceof FieldInsnNode) {
				FieldInsnNode fieldNode = (FieldInsnNode) instruction;
				if (fieldNode.owner.equals(oldOwner))
					fieldNode.owner = newOwner;
			} else if (instruction instanceof InvokeDynamicInsnNode) {
				InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;
				// I don't think these need to be remapped
				continue;
			}
		}
		
		newNode.name = oldMethod.name;
		newNode.desc = oldMethod.desc;
		newNode.access = oldMethod.access;
		
		newNode.signature = oldMethod.signature;
		newNode.exceptions = oldMethod.exceptions;
		
		if (newNode.localVariables != null)
			for (LocalVariableNode localVariable : newNode.localVariables)
				if (localVariable.desc.equals("L" + oldOwner + ";"))
					localVariable.desc = "L" + newOwner + ";";
		
		return newNode;
	}
}

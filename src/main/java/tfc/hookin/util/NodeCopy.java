package tfc.hookin.util;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.function.Function;

public class NodeCopy {
	public static MethodNode copy(MethodNode oldMethod, String oldOwner, String newOwner) {
		return copy(oldMethod, oldOwner, newOwner, (f) -> true);
	}
	
	public static MethodNode copy(MethodNode oldMethod, String oldOwner, String newOwner, Function<FieldInsnNode, Boolean> fieldRemapCondition) {
		MethodNode newNode = new MethodNode();
		oldMethod.accept(newNode);
		
		for (AbstractInsnNode instruction : newNode.instructions) {
			if (instruction instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) instruction;
				if (methodNode.owner.equals(oldOwner))
					methodNode.owner = newOwner;
			} else if (instruction instanceof FieldInsnNode) {
				FieldInsnNode fieldNode = (FieldInsnNode) instruction;
				if (fieldNode.owner.equals(oldOwner) && fieldRemapCondition.apply(fieldNode))
					fieldNode.owner = newOwner;
			} else if (instruction instanceof InvokeDynamicInsnNode) {
				InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;
				// I don't think these need to be remapped
				
				for (int i = 0; i < invokeDynamicInsnNode.bsmArgs.length; i++) {
					Object arg = invokeDynamicInsnNode.bsmArgs[i];
					if (arg instanceof Type) {
						// TODO
					} else if (arg instanceof Handle) {
						Handle handle = (Handle) arg;
						
						invokeDynamicInsnNode.bsmArgs[i] = new Handle(
								handle.getTag(),
								handle.getOwner().replace(oldOwner, newOwner),
								handle.getName(),
								handle.getDesc().replace(oldOwner, newOwner),
								handle.isInterface()
						);
					}
				}
				continue;
			} else if (instruction instanceof TypeInsnNode) {
				TypeInsnNode typeInsnNode = (TypeInsnNode) instruction;
				if (typeInsnNode.desc.equals(oldOwner))
					typeInsnNode.desc = newOwner;
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

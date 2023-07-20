package tfc.hookin.patches;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import tfc.hookin.TargetType;
import tfc.hookin.annotation.params.Point;
import tfc.hookin.internal.DescriptorUtils;
import tfc.hookin.patches.base.Patch;
import tfc.hookin.struct.template.params.MethodTargetStruct;
import tfc.hookin.struct.template.params.PointStruct;
import tfc.hookin.util.NodeCopy;
import tfc.hookin.util.tree.StackTracker;
import tfc.hookin.util.tree.nodes.CancelReturn;
import tfc.hookin.util.tree.nodes.HookLabel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

// TODO: preferably clean this up a bit
public class InjectPatch extends Patch<MethodNode> {
	MethodTargetStruct[] target;
	PointStruct injectionPoint;
	String srcClass;
	boolean cancellable;
	MethodNode method;
	
	String methodName;
	
	public InjectPatch(MethodTargetStruct[] target, PointStruct injectionPoint, boolean cancellable, ClassNode src, MethodNode methodNode) {
		super(TargetType.METHOD);
		this.target = target;
		this.injectionPoint = injectionPoint;
		this.method = methodNode;
		this.cancellable = cancellable;
		
		srcClass = src.name;
		this.methodName = method.name;
		this.method.name = "inject$" + srcClass.replace("/", "$") + "$_call_$" + methodName;
	}
	
	protected String swapDesc(String refDesc) {
		String type = ciType(refDesc);
		if (method.desc.contains(type)) {
			if (method.signature != null) {
				int i = method.signature.indexOf(type);
				String sub = method.signature.substring(i);
				
				String d = method.desc.replace("L" + type + ";", "");
				
				sub = sub.substring(sub.indexOf("<") + 1, sub.indexOf(">"));
				
				String ret = DescriptorUtils.extractReturnType(refDesc);
				char primitive = ret.charAt(0);
				if (primitive != 'L' && primitive != '[') sub = "" + primitive;
				
				return d.substring(0, d.lastIndexOf(")") + 1) + sub;
			}
		}
		return method.desc.replace("L" + type + ";", "");
	}
	
	@Override
	public boolean targets(MethodNode node) {
		for (MethodTargetStruct methodTarget : target)
			if (methodTarget.matches(
					swapDesc(node.desc),
					node
			)) return true;
		return false;
	}
	
	protected String ciType(String desc) {
		return desc.endsWith("V") ?
				"tfc/hookin/util/ci/CallInfo" :
				"tfc/hookin/util/ci/CallInfoReturnable"
				;
	}
	
	protected InsnList genCICreation(int local, MethodNode ref, boolean takeValue) {
		String ciType = ciType(ref.desc);
		
		InsnList list = new InsnList();
		// new CallInfo();
		list.add(new TypeInsnNode(Opcodes.NEW, ciType));
		if (takeValue) {
			list.add(new InsnNode(Opcodes.DUP_X1));
			list.add(new InsnNode(Opcodes.SWAP));
		} else {
			list.add(new InsnNode(Opcodes.DUP));
		}
		
		if (takeValue) {
			// find type
			char prim = 'L';
			String ret = DescriptorUtils.extractReturnType(ref.desc);
			if (ret.length() == 1) {
				prim = ret.charAt(0);
				ret = DescriptorUtils.getObjName(prim);
			} else ret = ret.substring(1, ret.length() - 1);
			
			// object
			if (prim != 'L')
				// primitive
				list.add(new MethodInsnNode(
						Opcodes.INVOKESTATIC,
						ret, "valueOf",
						"(" + prim + ")L" + ret + ";"
				));
		}
		
		list.add(new MethodInsnNode(
				Opcodes.INVOKESPECIAL,
				ciType,
				"<init>",
				takeValue ?
						"(Ljava/lang/Object;)V" :
						"()V"
		));
		
		if (cancellable) {
			list.add(new VarInsnNode(Opcodes.ASTORE, local));
			list.add(new VarInsnNode(Opcodes.ALOAD, local));
		}
		
		return list;
	}
	
	protected InsnList genCICancel(int local, MethodNode ref, boolean takeValue) {
		String ciType = ciType(ref.desc);
		
		InsnList list = new InsnList();
		
		// if (hookin$$CallInfo.cancelled) return [...];
		list.add(new VarInsnNode(Opcodes.ALOAD, local));
		list.add(new FieldInsnNode(
				Opcodes.GETFIELD,
				ciType,
				"cancelled",
				"Z"
		));
		
		LabelNode lbl = null;
		if (!takeValue) {
			Label label = new Label();
			method.visitLabel(label);
			lbl = new LabelNode(label);
			list.add(new JumpInsnNode(Opcodes.IFEQ, lbl));
		}
		
		// typed return
		if (!ref.desc.endsWith("V")) {
			// get return value
			list.add(new VarInsnNode(Opcodes.ALOAD, local));
			list.add(new FieldInsnNode(
					Opcodes.GETFIELD,
					ciType,
					"value",
					"Ljava/lang/Object;"
			));
			
			// find type
			char prim = 'L';
			String ret = DescriptorUtils.extractReturnType(ref.desc);
			if (ret.length() == 1) {
				prim = ret.charAt(0);
				ret = DescriptorUtils.getObjName(prim);
			} else ret = ret.substring(1, ret.length() - 1);
			
			// object
			list.add(new TypeInsnNode(
					Opcodes.CHECKCAST,
					ret
			));
			if (prim != 'L')
				// primitive
				list.add(new MethodInsnNode(
						Opcodes.INVOKEVIRTUAL,
						ret, DescriptorUtils.getPrimitiveName(prim) + "Value",
						"()" + prim
				));
		}
		if (!takeValue) {
			list.add(new CancelReturn(injectionPoint.getValue(), ref.desc));
			list.add(lbl);
		}
		
		return list;
	}
	
	protected InsnList genInject(HookLabel start, HookLabel end, MethodNode ref, ClassNode clazz, boolean takeValue) {
		MethodInsnNode methodInject = new MethodInsnNode(
				Modifier.isStatic(method.access) ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL,
				clazz.name,
				method.name,
				method.desc
		);
		
		int local = findLocal(start, end, ref);
		
		InsnList list = new InsnList();
		
		String d = method.desc.substring(1, method.desc.lastIndexOf(')'));
		
		int i = 0;
		while (!d.isEmpty()) {
			while (d.charAt(0) == '[') d = d.substring(1);
			
			switch (d.charAt(0)) {
				case 'L':
					String text = d.substring(0, d.indexOf(';'));
					
					if (text.equals("L" + ciType(ref.desc)))
						list.add(genCICreation(local, ref, takeValue));
					else
						list.add(new VarInsnNode(Opcodes.ALOAD, i));
					
					break;
				case 'B':
				case 'C':
				case 'S':
				case 'I':
					list.add(new VarInsnNode(Opcodes.ILOAD, i));
					break;
				case 'J':
					list.add(new VarInsnNode(Opcodes.LLOAD, i));
					break;
				case 'F':
					list.add(new VarInsnNode(Opcodes.FLOAD, i));
					break;
				case 'D':
					list.add(new VarInsnNode(Opcodes.DLOAD, i));
					break;
			}
			
			if (d.charAt(0) == 'L')
				d = d.substring(d.indexOf(';'));
			d = d.substring(1);
			
			i++;
		}
		
		list.add(methodInject);
		if (cancellable)
			list.add(genCICancel(local, ref, takeValue));
		
		return list;
	}
	
	@Override
	public int patch(ClassNode clazz, MethodNode node) {
		HookLabel start = null;
		HookLabel end = null;
		
		// find local variable reference labels
		for (AbstractInsnNode instruction : node.instructions) {
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				if (instruction instanceof HookLabel) {
					HookLabel label = (HookLabel) instruction;
					if (label.isStart()) start = label;
				}
			}
		}
		
		for (int i = node.instructions.size() - 1; i >= 0; i--) {
			AbstractInsnNode instruction = node.instructions.get(i);
			
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				if (instruction instanceof HookLabel) {
					HookLabel label = (HookLabel) instruction;
					if (!label.isStart()) end = label;
				}
			}
		}
		
		if (cancellable) {
			// add local variable reference labels if they're missing
			if (start == null) {
				Label label = new Label();
				node.visitLabel(label);
				node.instructions.insert(start = new HookLabel(label, true));
			}
			if (end == null) {
				Label label = new Label();
				node.visitLabel(label);
				node.instructions.add(end = new HookLabel(label, false));
			}
		}
		
		// do injection
		if (injectionPoint.getValue() == Point.Type.HEAD) {
			if (start != null)
				node.instructions.insert(start, genInject(start, end, node, clazz, false));
			else
				node.instructions.insert(genInject(null, end, node, clazz, false));
			
			return 1;
		} else if (
				injectionPoint.getValue() == Point.Type.RETURN ||
						injectionPoint.getValue() == Point.Type.FINAL_RETURN
		) {
			ArrayList<AbstractInsnNode> targets = new ArrayList<>();
			
			if (injectionPoint.getValue() == Point.Type.RETURN) {
				for (AbstractInsnNode instruction : node.instructions) {
					if (
							instruction.getOpcode() >= Opcodes.IRETURN &&
									instruction.getOpcode() <= Opcodes.RETURN
					) {
						targets.add(instruction);
					}
				}
			} else {
				for (int i = node.instructions.size() - 1; i >= 0; i--) {
					AbstractInsnNode instruction = node.instructions.get(i);
					if (
							instruction.getOpcode() >= Opcodes.IRETURN &&
									instruction.getOpcode() <= Opcodes.RETURN
					) {
						targets.add(instruction);
						break;
					}
				}
			}
			
			for (AbstractInsnNode abstractInsnNode : targets) {
				node.instructions.insertBefore(abstractInsnNode, genInject(start, end, node, clazz, true));
			}
			
			return targets.size();
		}
		
		List<AbstractInsnNode> targetNode = injectionPoint.selectInsn(node.instructions);
		
		int hits = 0;
		for (AbstractInsnNode target : targetNode) {
			InsnList methodInject = genInject(start, end, node, clazz, false);
			
			// TODO: number offset shifts?
			if (injectionPoint.getShift() == Point.Shift.AFTER)
				node.instructions.insert(target, methodInject);
			else if (injectionPoint.getShift() == Point.Shift.START && target instanceof MethodInsnNode)
				node.instructions.insertBefore(StackTracker.findStart(true, (MethodInsnNode) target, new int[1]), methodInject);
			else
				node.instructions.insertBefore(target, methodInject);
			
			hits++;
		}
		return hits;
	}
	
	@Override
	public void postApply(ClassNode node, int hits) {
		if (hits != 0)
			node.methods.add(NodeCopy.copy(this.method, srcClass, node.name));
	}
	
	
	// finds a CallInfo local, or creates one of none exists
	protected int findLocal(HookLabel start, HookLabel end, MethodNode ref) {
		String ciType = ciType(ref.desc);
		
		// TODO: scope tracking would be neat, but it seems painful
		int maxLocal = DescriptorUtils.countArgs(ref.desc);
		
		boolean returnable = ref.desc.endsWith("V");
		String sig = returnable ?
				"L" + ciType + "<" + DescriptorUtils.extractReturnType(ref.desc) + ">" + ";" :
				null;
		
		boolean foundOwn = false;
		for (LocalVariableNode localVariable : ref.localVariables) {
			if (localVariable.desc.equals("L" + ciType + ";")) {
				// skip if signature isn't right
				if (returnable)
					if (!sig.equals(localVariable.signature))
						continue;
				
				maxLocal = localVariable.index;
				foundOwn = true;
			}
		}
		
		if (!foundOwn) {
			
			// find how many locals are used in the method
			for (AbstractInsnNode instruction : ref.instructions)
				if (instruction.getType() == AbstractInsnNode.VAR_INSN)
					maxLocal = Math.max(((VarInsnNode) instruction).var + 1, maxLocal);
			
			// add LocalVariableNode
			if (maxLocal != -1) {
				if (ref.localVariables == null) {
					ref.localVariables = new ArrayList<>();
					ref.localVariables.add(
							new LocalVariableNode(
									"hookin$$CallInfo",
									"L" + ciType + ";",
									sig, start, end, maxLocal
							)
					);
				} else {
					boolean hasMyLocal = false;
					
					for (LocalVariableNode localVariable : ref.localVariables) {
						if (localVariable.index == maxLocal) {
							hasMyLocal = true;
							break;
						}
					}
					
					if (!hasMyLocal) {
						ref.localVariables.add(
								new LocalVariableNode(
										"hookin$$CallInfo",
										"L" + ciType + ";",
										sig, start, end, maxLocal
								)
						);
					}
				}
			}
			return maxLocal;
		}
		
		return -1;
	}
}

package tfc.hookin.patches;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import tfc.hookin.TargetType;
import tfc.hookin.patches.base.Patch;
import tfc.hookin.struct.template.params.MethodTargetStruct;
import tfc.hookin.util.NodeCopy;
import tfc.hookin.util.tree.element.MethodCall;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class RedirPatch extends Patch<MethodNode> {
	MethodTargetStruct target;
	String[] redirOwner;
	String[] redir;
	MethodTargetStruct[] exclude;
	
	final MethodNode node;
	String methodName;
	String srcClass;
	
	public RedirPatch(String src, MethodTargetStruct target, String[] redirs, MethodTargetStruct[] exclude, MethodNode node) {
		super(TargetType.METHOD);
		this.srcClass = src;
		
		this.target = target;
		
		this.redir = new String[redirs.length];
		this.redirOwner = new String[redirs.length];
		
		for (int i = 0; i < redirs.length; i++) {
			String redir = redirs[i];
			
			String[] split = redir.split(";", 2);
			this.redirOwner[i] = split[0].substring(1);
			this.redir[i] = split[1];
			if (!redir.contains("(")) {
				this.redir[i] = this.redir[i] + node.desc;
			}
		}
		
		this.exclude = exclude; // TODO: regex?
		
		this.node = node;
		this.methodName = node.name;
		this.node.name = "inject$" + srcClass.replace("/", "$") + "$_call_$" + methodName;
	}
	
	@Override
	public void postApply(ClassNode node, int hits) {
		if (hits != 0)
			node.methods.add(NodeCopy.copy(this.node, srcClass, node.name));
	}
	
	@Override
	public boolean targets(MethodNode node) {
		return target.matches(node.desc, node);
	}
	
	@Override
	public int patch(ClassNode classNode, MethodNode method) {
		int countHits = 0;
		
		for (MethodTargetStruct s : exclude)
			if (s.matches(null, method))
				return 0;
		
		HashMap<AbstractInsnNode, AbstractInsnNode> replacements = new HashMap<>();
		
		for (AbstractInsnNode instruction : method.instructions) {
			if (instruction instanceof MethodInsnNode) {
				MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
				
				for (String s : redir) {
					// TODO: check owner
					if ((methodInsnNode.name + methodInsnNode.desc).equals(s)) {
						MethodInsnNode newNode = new MethodInsnNode(
								Modifier.isStatic(node.access) ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL,
								classNode.name,
								node.name,
								node.desc,
								false
						);
						
						replacements.put(instruction, newNode);
						
						countHits++;
					}
				}
			}
		}
		
		for (Map.Entry<AbstractInsnNode, AbstractInsnNode> abstractInsnNodeAbstractInsnNodeEntry : replacements.entrySet()) {
			MethodInsnNode insn = (MethodInsnNode) abstractInsnNodeAbstractInsnNodeEntry.getKey();
			boolean hasThis = (insn.getOpcode() != Opcodes.INVOKESTATIC) && classNode.name.equals(insn.owner);
			if (!hasThis && !Modifier.isStatic(node.access)) {
				MethodCall methodCall = new MethodCall(false, classNode.name, (MethodInsnNode) abstractInsnNodeAbstractInsnNodeEntry.getKey());
				
				method.instructions.insertBefore(
						methodCall.start,
						new VarInsnNode(Opcodes.ALOAD, 0)
				);
			} else if (!Modifier.isStatic(node.access) && Modifier.isStatic(method.access)) {
				throw new RuntimeException("Static methods must be targetted using static hooks");
			}
			method.instructions.insertBefore(
					abstractInsnNodeAbstractInsnNodeEntry.getKey(),
					abstractInsnNodeAbstractInsnNodeEntry.getValue()
			);
			method.instructions.remove(abstractInsnNodeAbstractInsnNodeEntry.getKey());
		}
		
		return countHits;
	}
	
//	@Override
//	public String debugString() {
//		StringBuilder text = new StringBuilder("[");
//		for (int i = 0; i < redir.length; i++) text.append(redirOwner[i]).append("#").append(redir[i]);
//		text.append("]");
//		return "Redir(" + text + " in " + target.toString() + ")";
//	}
}

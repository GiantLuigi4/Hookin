package tfc.hookin.util.tree.element;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import tfc.hookin.util.tree.StackTracker;

import java.util.ArrayList;

public class MethodCall {
	public final AbstractInsnNode start;
	public final MethodInsnNode call;
	public final boolean hasThis;
	protected final Parameter[] params;
	
	public MethodCall(boolean findParams, String callerClass, MethodInsnNode insn) {
		this.call = insn;
		this.start = StackTracker.findStart(false, insn, new int[1]);
		
		if (findParams) {
			int size = StackTracker.getIO(insn)[1];
			this.params = new Parameter[size];
			int paramId = size;
			
			ArrayList<AbstractInsnNode> params = new ArrayList<>();
			
			AbstractInsnNode current = insn.getPrevious();
			
			// finds parameters
			while (current != start) {
				int[] io = StackTracker.getIO(current);
				if (size == paramId) {
					params.add(0, current);
					paramId--;
				}
				size += io[1] - io[0];
				
				current = current.getPrevious();
			}
			
			int[] io = StackTracker.getIO(current);
			
			if (size == paramId && io[1] == 0)
				params.add(0, current);
			
			AbstractInsnNode currentStart = start;
			for (int i = 0; i < this.params.length; i++) {
				AbstractInsnNode node = params.get(i);
				this.params[i] = new Parameter(
						null /* TODO: descriptor parsing */,
						currentStart, node
				);
				currentStart = node.getNext();
			}
		} else params = new Parameter[0];
		
		this.hasThis = (insn.getOpcode() != Opcodes.INVOKESTATIC) && callerClass.equals(insn.owner);
	}
	
	public Parameter getParameter(int i) {
		return params[i];
	}
	
	public int paramCount() {
		return params.length;
	}
}

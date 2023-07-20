package tfc.hookin.patches;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.hookin.patches.base.ClassPatch;
import tfc.hookin.struct.template.params.MethodTargetStruct;

import java.util.ArrayList;
import java.util.Arrays;

public class RemoveMethodPatch extends ClassPatch {
	MethodTargetStruct[] targets;
	
	public RemoveMethodPatch(MethodTargetStruct[] targets) {
		this.targets = targets;
	}
	
	@Override
	public int patch(ClassNode node) {
		ArrayList<MethodNode> toRemove = new ArrayList<>();
		
		for (MethodNode method : node.methods)
			for (MethodTargetStruct target : targets)
				if (target.matches(null, method))
					toRemove.add(method);
		
		for (MethodNode methodNode : toRemove)
			node.methods.remove(methodNode);
		
		return toRemove.size();
	}
	
//	@Override
//	public String debugString() {
//		return "RemoveMethods(" + Arrays.toString(targets) + ")";
//	}
}

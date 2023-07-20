package tfc.hookin.struct.template.params;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import tfc.hookin.annotation.params.Point;
import tfc.hookin.struct.BaseStruct;

import java.util.ArrayList;
import java.util.List;

public class PointStruct extends BaseStruct {
	Point.Type value;
	MethodTargetStruct methodTarget;
	Point.Shift shift;
	
	public List<AbstractInsnNode> selectInsn(InsnList instructions) {
		if (value == Point.Type.FIELD_GET || value == Point.Type.FIELD_SET)
			throw new RuntimeException("TODO");
		
		ArrayList<AbstractInsnNode> matches = new ArrayList<>();
		
		for (AbstractInsnNode instruction : instructions) {
			if (instruction instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) instruction;
				if (methodTarget.matches(
						methodNode.desc, methodNode.name, methodNode.desc
				)) {
					matches.add(methodNode);
				}
			}
		}
		
		return matches;
	}
	
	@Override
	public boolean handleNull(String key) {
		if (key.equals("shift")) {
			shift = Point.Shift.BEFORE;
			return true;
		}
		return false;
	}
	
	public Point.Type getValue() {
		return value;
	}
	
	public MethodTargetStruct getMethodTarget() {
		return methodTarget;
	}
	
	public Point.Shift getShift() {
		return shift;
	}
}

package tfc.hookin.util.tree.nodes;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import tfc.hookin.annotation.params.Point;
import tfc.hookin.internal.DescriptorUtils;

public class CancelReturn extends InsnNode {
	Point.Type pointType;
	
	protected static int getType(String desc) {
		String returnType = DescriptorUtils.extractReturnType(desc);
		switch (returnType.charAt(0)) {
			case 'V':
				return Opcodes.RETURN;
				
			case 'F':
				return Opcodes.FRETURN;
			case 'D':
				return Opcodes.DRETURN;
			case 'J':
				return Opcodes.LRETURN;
			
			case 'B':
			case 'C':
			case 'S':
			case 'I':
				return Opcodes.IRETURN;
		}
		return Opcodes.ARETURN;
	}
	
	public CancelReturn(Point.Type pointType, String desc) {
		super(getType(desc));
		this.pointType = pointType;
	}
	
	public Point.Type getPointType() {
		return pointType;
	}
}

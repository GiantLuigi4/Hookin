package tfc.hookin.patches;

import org.objectweb.asm.tree.ClassNode;
import tfc.hookin.patches.base.ClassPatch;
import tfc.hookin.struct.template.hooks.DummyStruct;

public class DummyPatch extends ClassPatch {
	DummyStruct struct;
	
	public DummyPatch(DummyStruct struct) {
		this.struct = struct;
	}
	
	public String dummyOf() {
		return struct.value;
	}
	
	@Override
	public int patch(ClassNode classNode) {
		classNode.methods.clear();
		classNode.fields.clear();
		return 1;
	}
	
//	@Override
//	public String debugString() {
//		return "Dummy(" + struct.value.toString().replace(".", "/") + ")";
//	}
}

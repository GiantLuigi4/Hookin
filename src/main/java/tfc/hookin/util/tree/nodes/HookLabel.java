package tfc.hookin.util.tree.nodes;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

public class HookLabel extends LabelNode {
	boolean start;
	
	public HookLabel(Label label, boolean start) {
		super(label);
		this.start = start;
	}
	
	public boolean isStart() {
		return start;
	}
}

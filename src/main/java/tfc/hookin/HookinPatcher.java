package tfc.hookin;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.hookin.patches.*;
import tfc.hookin.patches.base.ClassPatch;
import tfc.hookin.patches.base.InsnPatch;
import tfc.hookin.patches.base.Patch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HookinPatcher {
	// TODO: maybe a specially designed thing
	HashMap<String, PatchHolder> patches = new HashMap<>();
	
	ArrayList<Class<?>> patchOrder = new ArrayList<>();
	
	public HookinPatcher() {
		patchOrder.addAll(
				Arrays.asList(
						SwapSuperPatch.class,
						InjectPatch.class,
						RedirPatch.class,
						RemoveMethodPatch.class,
						DummyPatch.class
				)
		);
	}
	
	static class PatchHolder {
		HashMap<Class<?>, ArrayList<Patch<?>>> patches = new HashMap<>();
		
		public void add(Patch<?> patch) {
			ArrayList<Patch<?>> list = patches.computeIfAbsent(
					patch.getClass(),
					k -> new ArrayList<>()
			);
			
			list.add(patch);
		}
	}
	
	public void accept(HookParser.ParsedHook parsed) {
		for (String s : parsed.hook.value) {
			PatchHolder holder = this.patches.get(s.substring(1, s.length() - 1));
			if (holder == null) this.patches.put(s.substring(1, s.length() - 1), holder = new PatchHolder());
			for (Patch<?> patch : parsed.patches) {
				holder.add(patch);
			}
		}
	}
	
	public boolean patchClass(ClassNode node) {
		PatchHolder holder = patches.get(node.name);
		if (holder == null) return false;
		
		int[] hits = new int[1];
		
		for (Class<?> aClass : patchOrder) {
			List<Patch<?>> list = holder.patches.get(aClass);
			if (list == null) continue;
			
			for (Patch<?> patch : list) {
				
				if (patch.target == TargetType.CLASS) {
					int myHits = ((ClassPatch) patch).patch(node);
					
					hits[0] += myHits;
					patch.postApply(node, myHits);
				} else if (patch.target == TargetType.FIELD) {
					Patch<FieldNode> ptch = (Patch<FieldNode>) patch;
					
					int myHits = 0;
					for (FieldNode field : node.fields)
						if (ptch.targets(field))
							myHits += ptch.patch(node, field);
					hits[0] += myHits;
					ptch.postApply(node, myHits);
				} else if (patch.target == TargetType.METHOD) {
					Patch<MethodNode> ptch = (Patch<MethodNode>) patch;
					
					int myHits = 0;
					for (MethodNode method : node.methods)
						if (ptch.targets(method))
							myHits += ptch.patch(node, method);
					
					hits[0] += myHits;
					ptch.postApply(node, myHits);
				} else if (patch instanceof InsnPatch) {
					InsnPatch<AbstractInsnNode> ptch = (InsnPatch<AbstractInsnNode>) patch;
					
					int myHits = 0;
					for (MethodNode method : node.methods)
						for (AbstractInsnNode instruction : method.instructions)
							if (ptch.targets(instruction))
								myHits += ptch.patch(node, method, instruction);
					
					hits[0] += myHits;
					ptch.postApply(node, myHits);
				}
			}
		}
		
		return hits[0] > 0;
	}
}

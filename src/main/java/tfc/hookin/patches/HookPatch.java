package tfc.hookin.patches;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.hookin.annotation.hinting.MergeStatic;
import tfc.hookin.annotation.hinting.Shadow;
import tfc.hookin.patches.base.ClassPatch;
import tfc.hookin.struct.HookStruct;
import tfc.hookin.util.AnnotationParser;
import tfc.hookin.util.NodeCopy;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class HookPatch extends ClassPatch {
	HookStruct struct;
	ClassNode clazz;
	ArrayList<MethodNode> shadowedMethods = new ArrayList<>();
	ArrayList<FieldNode> shadowedFields = new ArrayList<>();
	
	public HookPatch(HookStruct struct, ClassNode clazz) {
		this.struct = struct;
		this.clazz = clazz;
		
		for (MethodNode method : clazz.methods)
			for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(method))
				if (AnnotationParser.isAnnotation(Shadow.class, allAnnotation))
					shadowedMethods.add(method);
		
		clazz.methods.removeAll(shadowedMethods);
		
		for (FieldNode fieldNode : clazz.fields)
			for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(fieldNode))
				if (AnnotationParser.isAnnotation(Shadow.class, allAnnotation))
					shadowedFields.add(fieldNode);
		
		clazz.fields.removeAll(shadowedFields);
	}
	
	protected String remap(String desc, String oldSuper, String newSuper) {
		return desc.replace("L" + oldSuper + ";", "L" + newSuper + ";");
	}
	
	@Override
	public int patch(ClassNode clazz) {
		// merge interfaces
		if (this.clazz.interfaces != null) {
			if (clazz.interfaces == null)
				clazz.interfaces = new ArrayList<>();
			
			clazz.interfaces.addAll(this.clazz.interfaces);
		}
		
		int hits = clazz.methods.size();
		String oldSuper = this.clazz.name;
		String newSuper = clazz.name;
		
		if (struct.merge) {
			// merge methods
			for (MethodNode method : this.clazz.methods) {
//			boolean hasOverride = false;
//			for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(method))
//				if (AnnotationParser.isAnnotation(Override.class, allAnnotation))
//					hasOverride = true;
				
				// TODO: rename method if it's not an override
				clazz.methods.add(NodeCopy.copy(
						method, oldSuper, newSuper, (field) -> {
							for (FieldNode fieldNode : this.clazz.fields) {
								if (!AnnotationParser.hasAnnotation(MergeStatic.class, fieldNode)) {
									if (
											fieldNode.name.equals(field.name) &&
													fieldNode.desc.equals(field.desc)
									) return false;
								}
							}
							
							return true;
						}
				));
			}
			
			// merge fields
			for (FieldNode field : this.clazz.fields)
				// TODO: rename field
				if (!Modifier.isStatic(field.access) || AnnotationParser.hasAnnotation(MergeStatic.class, field))
					clazz.fields.add(field);
		}
		
		return hits;
	}
}

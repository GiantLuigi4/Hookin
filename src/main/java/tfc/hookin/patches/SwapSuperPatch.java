package tfc.hookin.patches;

import org.objectweb.asm.tree.*;
import tfc.hookin.annotation.hinting.ShadowOverride;
import tfc.hookin.patches.base.ClassPatch;

public class SwapSuperPatch extends ClassPatch {
    String newSuper;

    public SwapSuperPatch(String newSuper) {
        this.newSuper = newSuper;
    }

    protected String remap(String desc, String oldSuper) {
        return desc.replace("L" + oldSuper + ";", "L" + newSuper + ";");
    }

    @Override
    public int patch(ClassNode clazz) {
        boolean changed = !clazz.superName.equals(newSuper);
        int hits = 0;
        if (changed) {
            String oldSuper = clazz.superName;
            clazz.superName = newSuper;
            hits = 1;

            // remap methods
            for (MethodNode method : clazz.methods) {
                method.desc = remap(method.desc, oldSuper);
                if (method.signature != null) method.signature = remap(method.signature, oldSuper);

                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode minsin = (MethodInsnNode) instruction;
                        
                        if (minsin.owner.equals(oldSuper))
                            minsin.owner = newSuper;
                        minsin.desc = remap(minsin.desc, oldSuper);
                    } // TODO: invoke dynamic?
                }
            }
            
            // remap fields
            for (FieldNode field : clazz.fields) {
                field.desc = remap(field.desc, oldSuper);
                if (field.signature != null) field.signature = remap(field.signature, oldSuper);
            }
        }

        // swap ShadowOverrides for Overrides
        for (MethodNode method : clazz.methods) {
            if (method.invisibleAnnotations != null)
                for (AnnotationNode invisibleAnnotation : method.invisibleAnnotations)
                    if (invisibleAnnotation.desc.equals(
                            "L" + ShadowOverride.class.getName().replace(".", "/") + ";"
                    )) invisibleAnnotation.desc = "L" + Override.class.getName().replace(".", "/") + ";";

            if (method.visibleAnnotations != null)
                for (AnnotationNode invisibleAnnotation : method.visibleAnnotations)
                    if (invisibleAnnotation.desc.equals(
                            "L" + ShadowOverride.class.getName().replace(".", "/") + ";"
                    )) invisibleAnnotation.desc = "L" + Override.class.getName().replace(".", "/") + ";";
        }

        return hits;
    }
}

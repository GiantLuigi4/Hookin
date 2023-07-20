package tfc.hookin.patches.base;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.hookin.TargetType;

public abstract class InsnPatch<T extends AbstractInsnNode> extends Patch<T> {
    public InsnPatch(TargetType<T> target) {
        super(target);
    }

    @Override
    public int patch(ClassNode clazz, T node) {
        throw new RuntimeException("Cannot run InsnPatch without a method target");
    }

    public abstract int patch(ClassNode clazz, MethodNode method, T node);
}

package tfc.hookin.patches.base;

import org.objectweb.asm.tree.ClassNode;
import tfc.hookin.TargetType;

public abstract class ClassPatch extends Patch<ClassNode> {
    public ClassPatch() {
        super(TargetType.CLASS);
    }

    @Override
    public final int patch(ClassNode clazz, ClassNode node) {
        return patch(node);
    }

    @Override
    public boolean targets(ClassNode node) {
        return true; // should never get here anyway
    }

    public abstract int patch(ClassNode clazz);
}

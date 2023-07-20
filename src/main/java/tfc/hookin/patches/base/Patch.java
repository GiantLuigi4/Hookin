package tfc.hookin.patches.base;

import org.objectweb.asm.tree.ClassNode;
import tfc.hookin.TargetType;

public abstract class Patch<T> {
    public final TargetType<T> target;

    public Patch(TargetType<T> target) {
        this.target = target;
    }

    public abstract boolean targets(T node);

    public abstract int patch(ClassNode clazz, T node);
    
    public void postApply(ClassNode node, int hits) {
    }
}

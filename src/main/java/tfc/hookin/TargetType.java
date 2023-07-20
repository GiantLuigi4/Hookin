package tfc.hookin;

import org.objectweb.asm.tree.*;

public class TargetType<T> {
    public static final TargetType<ClassNode> CLASS = new TargetType<>(ClassNode.class);
    public static final TargetType<MethodNode> METHOD = new TargetType<>(MethodNode.class);
    public static final TargetType<FieldNode> FIELD = new TargetType<>(FieldNode.class);
    public static final TargetType<InsnNode> INSN = new TargetType<>(InsnNode.class);
    public static final TargetType<FieldInsnNode> FIELD_INSN = new TargetType<>(FieldInsnNode.class);
    public static final TargetType<MethodInsnNode> METHOD_INSN = new TargetType<>(MethodInsnNode.class);
    
    public final Class<?> clazz;
    
    public TargetType(Class<?> clazz) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (!elements[2].getClassName().equals(TargetType.class.getName())) {
            throw new RuntimeException("no");
        }
        this.clazz = clazz;
    }
}

package test.hooks;

import test.HookTarget;
import tfc.hookin.annotation.Hook;
import tfc.hookin.annotation.hinting.MergeStatic;
import tfc.hookin.annotation.hinting.Shadow;
import tfc.hookin.annotation.hooks.Inject;
import tfc.hookin.annotation.hooks.MethodRedir;
import tfc.hookin.annotation.params.MethodTarget;
import tfc.hookin.annotation.params.Point;
import tfc.hookin.util.ci.CallInfo;
import tfc.hookin.util.ci.CallInfoReturnable;

import java.util.function.Supplier;

@Hook(HookTarget.class)
public class TestHookin {
	// static fields aren't merged by default
	// unmerged fields must not be private
	// this can be used to access private static fields, constructors, etc
	public static Supplier<HookTarget> constructor = () -> (HookTarget) (Object) new TestHookin();
	
	
	// the MergeStatic annotation can be used to merge a static field into the target
	// for single target hookins, this doesn't really make a difference
	// however, for multi target hookins, this will make a large difference
	// what's ideal for you, depends on use case
	@MergeStatic
	public static int field = 42;
	
	
	// on methods, the default behavior is merging
	// however, due to how access modifiers work, there isn't too much in a point in not merging them
	// so for now, there's no way to override that behavior other than entirely turning off merging
	public static void toMergeIn() {
		System.err.println("From merged");
		System.err.println(field);
		field += 4;
		System.err.println(field);
	}
	
	
	// shadow behaves the same as how it behaves in mixin
	// the shadowed method/field/constructor gets skipped over
	// instead, it works as hinting to remap to the target
	
	//@formatter:off
	@Shadow public static native void toShadow(int number);
	@Shadow public TestHookin() {}
	//@formatter:on
	
	
	// from here, if you know how mixin works, you can probably figure out all of this
	
	@Inject(target = @MethodTarget("main"), point = @Point(Point.Type.HEAD), cancellable = true)
	public static void preMain(String[] args, CallInfo ci) {
		System.out.println("hi");
	}
	
	
	@Inject(target = @MethodTarget("returnableMethod"), point = @Point(Point.Type.HEAD), cancellable = true)
	public static void preReturnableMethod(CallInfoReturnable<Object> ci) {
		ci.value = null;
		ci.cancelled = true;
	}
	
	@Inject(
			target = @MethodTarget("returnableMethod2"),
			point = @Point(value = Point.Type.INVOCATION, methodTarget = @MethodTarget(value = "returnableMethod"), shift = Point.Shift.BEFORE),
			cancellable = true
	)
	public static void preRunMethod(CallInfoReturnable<Float> ci) {
		ci.value = 32f;
		ci.cancelled = true;
	}
	
	@Inject(target = @MethodTarget("aMethodThatDoesImportantCalculations"), point = @Point(Point.Type.RETURN), cancellable = true)
	public static void postDoStuff(CallInfoReturnable<Integer> ci) {
		System.out.println(ci.value);
		ci.value -= 3;
		
		toShadow(32);
		System.out.println(new TestHookin());
		toMergeIn();
	}
	
	@MethodRedir(target = @MethodTarget("main"), redirect = "Ltest/HookTarget;printLn")
	public static void redirPrint(String arg) {
		System.out.println(arg);
	}
}

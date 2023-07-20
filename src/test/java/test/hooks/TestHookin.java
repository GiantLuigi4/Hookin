package test.hooks;

import test.HookTarget;
import tfc.hookin.annotation.Hook;
import tfc.hookin.annotation.hooks.Inject;
import tfc.hookin.annotation.hooks.MethodRedir;
import tfc.hookin.annotation.params.MethodTarget;
import tfc.hookin.annotation.params.Point;
import tfc.hookin.util.ci.CallInfo;
import tfc.hookin.util.ci.CallInfoReturnable;

@Hook(HookTarget.class)
public class TestHookin {
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
	
	@MethodRedir(target = @MethodTarget("main"), redirect = "Ltest/HookTarget;printLn")
	public static void redirPrint(String arg) {
		System.out.println(arg);
	}
	
	@Inject(target = @MethodTarget("aMethodThatDoesImportantCalculations"), point = @Point(Point.Type.RETURN), cancellable = true)
	public static void postDoStuff(CallInfoReturnable<Integer> ci) {
		System.out.println(ci.value);
		
		ci.value -= 3;
	}
}

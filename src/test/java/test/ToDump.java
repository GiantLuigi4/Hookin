package test;

import tfc.hookin.util.ci.CallInfo;

public class ToDump {
	public static void main(String[] args) {
		CallInfo hookin$$CallInfo = new CallInfo();
		inject$test$hooks$TestHookin$_call_$preMain(args, hookin$$CallInfo);
		if (hookin$$CallInfo.cancelled) return;
		
		System.out.println("Test");// 5
	}
	
	public static void inject$test$hooks$TestHookin$_call_$preMain(String[] args, CallInfo ci) {
		System.out.println("hi");// 15
		ci.cancelled = true;// 17
	}
}

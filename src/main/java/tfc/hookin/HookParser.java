package tfc.hookin;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.hookin.annotation.Hook;
import tfc.hookin.annotation.hooks.Inject;
import tfc.hookin.annotation.hooks.MethodRedir;
import tfc.hookin.annotation.hooks.SwapSuper;
import tfc.hookin.patches.InjectPatch;
import tfc.hookin.patches.RedirPatch;
import tfc.hookin.patches.SwapSuperPatch;
import tfc.hookin.patches.base.Patch;
import tfc.hookin.struct.HookStruct;
import tfc.hookin.struct.template.hooks.InjectStruct;
import tfc.hookin.struct.template.hooks.MethodRedirStruct;
import tfc.hookin.struct.template.hooks.SwapSuperStruct;
import tfc.hookin.util.AnnotationParser;
import tfc.hookin.util.TriFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

public class HookParser {
	private HashMap<String, HookFactory<?, ?, ?, ?>> HOOK_REGISTRY = new HashMap<>();
	
	public HookParser() {
		registerHookType(
				Inject.class,
				InjectStruct.class,
				(struct, clazz, node) -> new InjectPatch(
						struct.target(),
						struct.injectionPoint(),
						struct.isCancellable(),
						clazz, (MethodNode) node
				)
		);
		registerHookType(
				SwapSuper.class,
				SwapSuperStruct.class,
				(struct, clazz, node) -> new SwapSuperPatch(
						struct.value
				)
		);
		registerHookType(
				MethodRedir.class,
				MethodRedirStruct.class,
				(struct, clazz, node) -> new RedirPatch(
						clazz.name,
						struct.target,
						struct.redirect,
						struct.exclude,
						(MethodNode) node
				)
		);
	}
	
	public <STRUCT, NODE_TYPE, GENERATOR extends Patch<?>> void registerHookType(
			Class<?> annotation,
			Class<STRUCT> structType,
			TriFunction<STRUCT, ClassNode, NODE_TYPE, GENERATOR> generator
	) {
		HOOK_REGISTRY.put(
				"L" + annotation.getName().replace(".", "/") + ";",
				new HookFactory<>(structType, generator)
		);
	}
	
	protected static class HookFactory<T, E, Q, V extends Patch<?>> {
		Class<T> structType;
		TriFunction<T, E, Q, V> generator;
		
		public HookFactory(Class<T> structType, TriFunction<T, E, Q, V> generator) {
			this.structType = structType;
			this.generator = generator;
		}
		
		public V accept(AnnotationNode node, ClassNode clazz, Object nd) {
			//noinspection unchecked
			return generator.apply(
					AnnotationParser.parseInto(node, structType),
					(E) clazz, (Q) nd
			);
		}
	}
	
	public static class ParsedHook {
		HookStruct hook;
		ArrayList<Patch<?>> patches;
		
		public ParsedHook(HookStruct hook, ArrayList<Patch<?>> patches) {
			this.hook = hook;
			this.patches = patches;
		}
	}
	
	public ParsedHook parse(ClassNode clazz) {
		ArrayList<Patch<?>> allPatches = new ArrayList<>();
		
		HookStruct hook = null;
		
		for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(clazz)) {
			if (AnnotationParser.isAnnotation(
					Hook.class, allAnnotation
			)) hook = AnnotationParser.parseInto(allAnnotation, HookStruct.class);
			
			HookFactory<?, ?, ?, ?> factory = HOOK_REGISTRY.get(allAnnotation.desc);
			if (factory != null)
				allPatches.add(factory.accept(
						allAnnotation,
						clazz, clazz
				));
		}
		
		for (MethodNode method : clazz.methods) {
			for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(method)) {
				HookFactory<?, ?, ?, ?> factory = HOOK_REGISTRY.get(allAnnotation.desc);
				if (factory != null)
					allPatches.add(factory.accept(
							allAnnotation,
							clazz, method
					));
			}
		}
		
		for (FieldNode field : clazz.fields) {
			for (AnnotationNode allAnnotation : AnnotationParser.allAnnotations(field)) {
				HookFactory<?, ?, ?, ?> factory = HOOK_REGISTRY.get(allAnnotation.desc);
				if (factory != null)
					allPatches.add(factory.accept(
							allAnnotation,
							clazz, field
					));
			}
		}
		
		return new ParsedHook(hook, allPatches);
	}
}

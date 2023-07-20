import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import tfc.flame.loader.IFlameLoader;
import tfc.flame.loader.util.JDKLoader;
import tfc.hookin.util.tree.BytecodeWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class TestInvoker {
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
		ClassLoader loader = (ClassLoader) JDKLoader.createLoader(new URL[0], TestInvoker.class.getClassLoader(), true);
		((IFlameLoader) loader).addOverridePath(new File("").getAbsolutePath());
		Thread.currentThread().setContextClassLoader(loader);
		
		
		
		ClassReader reader = new ClassReader(((IFlameLoader) loader).getBytecode(
				"test.ToDump", true, null
		));
		ClassNode nd = new ClassNode();
		reader.accept(nd, ClassReader.EXPAND_FRAMES);
		
		BytecodeWriter.write(
				nd, new File("dump.txt")
		);
		
		
		
		//noinspection UnusedAssignment
		Class<?> clazz = loader.loadClass("test.HookinSetup");
		clazz.getMethod("init").invoke(null);
		
		clazz = loader.loadClass("test.HookTarget");
		clazz.getMethod("main", String[].class)
				.invoke(null, (Object) args);
	}
}

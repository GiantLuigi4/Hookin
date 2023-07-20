package tfc.hookin.annotation.hooks.dangerous;

import tfc.hookin.annotation.params.MethodTarget;

/**
 * removes methods from a class
 * there is pretty much no valid use case for this
 */
public @interface RemoveMethods {
	MethodTarget[] targets();
}

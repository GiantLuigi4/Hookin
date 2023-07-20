package tfc.hookin.struct.template.hooks;

import tfc.hookin.struct.BaseStruct;
import tfc.hookin.struct.template.params.MethodTargetStruct;
import tfc.hookin.struct.template.params.PointStruct;

public class InjectStruct extends BaseStruct {
	MethodTargetStruct[] target;
	PointStruct point;
	boolean cancellable;
	
	@Override
	public boolean handleNull(String key) {
		if (key.equals("cancellable"))
			cancellable = false;
		
		return false;
	}
	
	public MethodTargetStruct[] target() {
		return target;
	}
	
	public PointStruct injectionPoint() {
		return point;
	}
	
	public boolean isCancellable() {
		return cancellable;
	}
}

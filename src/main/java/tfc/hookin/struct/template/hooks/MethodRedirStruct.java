package tfc.hookin.struct.template.hooks;

import tfc.hookin.struct.BaseStruct;
import tfc.hookin.struct.template.params.MethodTargetStruct;

public class MethodRedirStruct extends BaseStruct {
	public MethodTargetStruct target;
	public String[] redirect;
	public MethodTargetStruct[] exclude;
	
	public boolean handleNull(String key) {
		if (key.equals("target")) {
			target = new MethodTargetStruct();
			
			return true;
		} else if (key.equals("exclude")) {
			exclude = new MethodTargetStruct[0];
			return true;
		}
		return false;
	}
}

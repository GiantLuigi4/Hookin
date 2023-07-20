package tfc.hookin.struct;

public class HookStruct extends BaseStruct {
	public String[] value;
	public boolean merge;
	
	@Override
	public boolean handleNull(String key) {
		if (key.equals("merge"))
			return merge = true;
		return false;
	}
}

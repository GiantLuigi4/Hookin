package tfc.hookin.util.ci;

public class CallInfoReturnable<T> {
	public boolean cancelled = false;
	public T value;
	
	public CallInfoReturnable() {
	}
	
	public CallInfoReturnable(T value) {
		this.value = value;
	}
}

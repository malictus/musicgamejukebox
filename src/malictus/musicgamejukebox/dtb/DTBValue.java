package malictus.musicgamejukebox.dtb;

public class DTBValue {

	//float, string, or integer object (single only) or sometimes null
	Object value;
	int type;

	public DTBValue(Object value, int type) {
		this.value = value;
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public int getType() {
		return type;
	}

}

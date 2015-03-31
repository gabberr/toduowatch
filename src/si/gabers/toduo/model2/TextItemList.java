package si.gabers.toduo.model2;

import com.google.gson.annotations.Expose;

public class TextItemList implements ItemListInterface {
	@Expose
	String name;
	@Expose
	boolean ticked;
	public TextItemList(String _name) {
		name = _name;
		ticked = false;
	}
	@Override
	public String getItemName() {
		return name;
	}

	@Override
	public boolean isImageItem() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String toString() {
		return name;
	}
	@Override
	public boolean isTicked() {
		return ticked;
	}
	
	public void setTicked(boolean value) {
		ticked = value;
	}


}

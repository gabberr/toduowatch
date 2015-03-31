package si.gabers.toduo.model2;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class ItemRootElement {
	@Expose
	public ArrayList<MainItemListModel> items;
	public ItemRootElement() {
		items = new ArrayList<MainItemListModel>();
	}

}

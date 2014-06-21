package si.gabers.toduo.model;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class MainItemListModel{
	@Expose
	String listName;
	@Expose
	ArrayList<ItemListInterface> items;
	
	public MainItemListModel(String name) {
		listName=name;
		items = new ArrayList<ItemListInterface>();
	}
	public void addItem(ItemListInterface item) {
		items.add(item);
	}
	public void removeItem(ItemListInterface item) {
		items.remove(item);
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return listName;
	}
	public ArrayList<ItemListInterface> getItemList() {
//		ArrayList<String> list = new ArrayList<>();
//		
//		for(int i = 0; i< items.size(); i++)
//			list.add(items.get(i).getItemName());
		return items;
	}
	
	
}

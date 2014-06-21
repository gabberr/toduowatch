package si.gabers.toduo.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public interface ItemListInterface {
	String getItemName();
	boolean isImageItem();
	boolean isTicked();
	public void setTicked(boolean value);
}

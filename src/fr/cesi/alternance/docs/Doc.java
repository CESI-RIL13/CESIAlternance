package fr.cesi.alternance.docs;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.JsonWriter;

import fr.cesi.alternance.helpers.Entity;

public class Doc extends Entity {

	private String name;
	private String description;
	private String path;
	private boolean selected;

	public Doc(){

	}

	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}





	@Override
	public String getApiPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Doc fromJSON(JSONObject json) {
		try {
			this.id = json.getInt("id");
			this.name = json.getString("name");
			this.description = json.getString("description");
			this.path = json.getString("path");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;

	}

	@Override
	public JSONObject asJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("name", this.name);
			json.put("description", this.description);
			json.put("path", this.path);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

}

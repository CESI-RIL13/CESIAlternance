package fr.cesi.alternance.helpers;

import org.json.JSONObject;

import fr.cesi.alternance.Constants;

public abstract class Entity {
	
	public static final String TAG 			= Constants.APP_NAME + ".Entity";
	
	protected long id;

	public long getId() {
		return this.id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public abstract String getApiPath();
	public abstract Entity fromJSON(JSONObject json);
	public abstract JSONObject asJSON();

}

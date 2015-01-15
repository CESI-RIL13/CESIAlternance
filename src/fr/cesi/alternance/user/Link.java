package fr.cesi.alternance.user;

import org.json.JSONException;
import org.json.JSONObject;

import fr.cesi.alternance.helpers.Entity;

public class Link extends Entity{
	private TypeEnum type;
	private String url;
	
	/**
	 * @param id
	 * @param type
	 * @param url
	 */
	public Link(int id, TypeEnum type, String url) {
		this.id = id;
		this.type = type;
		this.url = url;
	}
	
	public Link(){
		
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	public TypeEnum getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(TypeEnum type) {
		this.type = type;
	}
	
	@Override
	public String getName() {
		return url;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getApiPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Link fromJSON(JSONObject json) {
		// TODO Auto-generated method stub
		try {
			id=json.getLong("id");
			type= TypeEnum.fromString(json.getString("type"));
			url=json.getString("url");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public JSONObject asJSON() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "link :" + url + ", type :" + type.value();
	}
}

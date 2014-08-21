package fr.cesi.alternance.establishment;

import org.json.JSONException;
import org.json.JSONObject;

public class Establishment {
	private int id;
	private String name, adresse, phone, fax, alias;
	
	public Establishment(){
		
	}
	
	public Establishment(int id, String name, String adresse, String phone, String fax, String alias){
		this.id = id;
		this.name = name;
		this.adresse = adresse;
		this.phone = phone;
		this.fax = fax;
		this.alias = alias;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	public Establishment fromJSON(JSONObject json) {
		try {
			this.id = json.getInt("id");
			this.name = json.getString("name");
			this.adresse = json.getString("adresse");
			this.phone = json.getString("phone");
			this.fax = json.getString("fax");
			this.alias = json.getString("alias");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}
}


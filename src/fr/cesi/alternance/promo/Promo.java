package fr.cesi.alternance.promo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.helpers.Entity;
import fr.cesi.alternance.user.User;

public class Promo extends Entity implements Parcelable {
	
	private int promo_id;
	private String name;
	private int number;
	private String code;
	
	
	public Promo(){}
	
	public Promo(long id) {
		this.id = id;
	}
	
	public Promo(long id, String name, int number, String code) {
		this.id = id;
		this.name = name;
		this.number = number;
		this.code = code;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getApiPath() {
		String api_path = "promo/" + this.id;
		return api_path;
	}

	public Promo(Parcel in){
		id = in.readLong();
		name = in.readString();
		number = in.readInt();
		code = in.readString();
	}

	@Override
	public Promo fromJSON(JSONObject json) {
		try {
			id = json.getLong("id");
			number = json.getInt("number");
			name = json.getString("name");
			code = json.getString("code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public JSONObject asJSON() {
		return null;
	}

	public ArrayList<User> getListUser(String role) {
		ArrayList<User> user_list = new ArrayList<User>();
		// Remplacer par list d'�l�ve ou intervenant
		String url = Constants.BASE_API_URL + "/user/listUser";
		try {
			// Prod :
			String token = AccountHelper.getData(Api.UserColumns.TOKEN);
			// Debug :
			//String token = Constants.DEBUG_APP_AUTH_TOKEN;
			
			JSONObject json = new HttpData(url).header(Api.APP_AUTH_TOKEN, token).data("promo", String.valueOf(id)).data("role", role).get().asJSONObject();
			if(json.getBoolean("success")) {
				JSONArray result = json.getJSONArray("result");
				for (int i = 0; i < result.length(); i++) {
					User e = new User().fromJSON(result.getJSONObject(i));
					user_list.add(e);
				}
			}
		} catch (HttpDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return user_list;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeInt(number);
		dest.writeString(code);
	}

	public int getPromoId() {
		return promo_id;
	}

	public static final Parcelable.Creator<Promo> CREATOR = new Parcelable.Creator<Promo>() {
		public Promo createFromParcel(Parcel in) {
			return new Promo(in);
		}

		public Promo[] newArray(int size) {
			return new Promo[size];
		}
	};
}

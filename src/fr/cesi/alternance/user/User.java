package fr.cesi.alternance.user;

import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.helpers.Entity;
//import fr.cesi.alternance.Promo;

public class User extends Entity implements Parcelable {

	private String name;
	private String mail;
	private String role;
	private String phone;
	private int id_promo;

	public User() {
	}

	public User(Parcel in){
		id = in.readLong();
		name = in.readString();
		mail = in.readString();
		role = in.readString();
		phone = in.readString();
	}

	@Override
	public String getApiPath() {
		String api_path = "user/" + this.id;
		return api_path;
	}

	@Override
	public User fromJSON(JSONObject json) {
		try {
			id = json.getLong("id");
			name = json.getString("name");
			mail = json.getString("email");
			role = json.getString("role");
			phone = json.getString("phone");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public JSONObject asJSON() {
		return null;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	public int getId_promo() {
		return id_promo;
	}

	public void setId_promo(int id_promo) {
		this.id_promo = id_promo;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(mail);
		dest.writeString(role);
		dest.writeString(phone);
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	//fonction qui fait la requète submit
	private boolean save(){

		boolean success = false;
		try {
			//requète 
			//appel de la fonction addUser
			String url = Constants.BASE_API_URL + "/user/" + (this.id > 0 ? "updateUser" : "addUser");
			//Authentification
			String token = AccountHelper.getData(Api.UserColumns.TOKEN);
			HttpData post = new HttpData(url).header(Api.APP_AUTH_TOKEN,token)
					.data("name",name)
					.data("email",mail)
					.data("role",role)
					.data("phone",phone)
					.data("id_promo", "" + id_promo)
					.post();
			JSONObject obj = post.asJSONObject();
			success = obj.getBoolean("success");
			if(success) {
				JSONObject rs = obj.getJSONObject("result");
				setId(rs.getLong("id"));
			}

		} catch (HttpDataException hde) {
			// TODO Auto-generated catch block
			hde.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}
}
package fr.cesi.alternance.user;

import org.json.JSONException;
import org.json.JSONObject;
import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;
import android.os.Parcel;
import android.os.Parcelable;
import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.helpers.Entity;

public class User extends Entity implements Parcelable {

	private String name;
	private String mail;
	private String role;
	private String phone;
	private String picture_path;
	private long id_promo;

	public User() {}

	public User(Parcel in){
		id = in.readLong();
		name = in.readString();
		mail = in.readString();
		role = in.readString();
		phone = in.readString();
		picture_path = in.readString();
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
			picture_path = json.getString("picture_path");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public JSONObject asJSON() {
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getPicture_path() {
		return picture_path;
	}

	public void setPicture_path(String picture_path) {
		this.picture_path = picture_path;
	}

	public long getId_promo() {
		return id_promo;
	}

	public void setId_promo(long id_promo) {
		this.id_promo = id_promo;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(mail);
		dest.writeString(role);
		dest.writeString(phone);
		dest.writeString(picture_path);
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	public boolean save(){

		boolean success = false;
		try {
			//requ�te 
			//appel de la fonction addUser
			String url = Constants.BASE_API_URL + "/user/" + (this.id > 0 ? "updateUser" : "addUser");
			//Authentification
			String token = AccountHelper.getData(Api.UserColumns.TOKEN);
			HttpData post = new HttpData(url).header(Api.APP_AUTH_TOKEN,token)
					.data("id",""+id)
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
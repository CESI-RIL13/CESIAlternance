package fr.cesi.alternance.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import android.accounts.AuthenticatorException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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
	private String photo;
	private ArrayList<Link> links;
	private int id_promo;

	public User() {
		links = new ArrayList<Link>();
	}

	public User(Parcel in){
		id = in.readLong();
		name = in.readString();
		mail = in.readString();
		role = in.readString();
		phone = in.readString();
		photo=in.readString();
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
			photo=json.getString("picture_path");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public static User fromBundle(Bundle bundle){
		User u = new User();
		u.id= Integer.parseInt(bundle.getString(Api.UserColumns.ID));
		u.name=bundle.getString(Api.UserColumns.NAME);
		u.mail=bundle.getString(Api.UserColumns.EMAIL);
		u.role=bundle.getString(Api.UserColumns.ROLE);
		u.phone=bundle.getString(Api.UserColumns.PHONE);
		u.photo=bundle.getString(Api.UserColumns.PICTURE_PATH);
		String listeLinks = bundle.getString(Api.UserColumns.LINKS);
		try {
			JSONArray jsLinks = new JSONArray(listeLinks);
			for (int i = 0; i < jsLinks.length(); i++) {
				Link link = new Link().fromJSON(jsLinks.getJSONObject(i));
				u.links.add(link);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return u;
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

	/**
	 * @return the photo
	 */
	public String getPhoto() {
		return photo;
	}

	/**
	 * @param photo the photo to set
	 */
	public void setPhoto(String photo) {
		this.photo = photo;
	}

	/**
	 * @return the links
	 */
	public ArrayList<Link> getLinks() {
		if(links == null) links = new ArrayList<Link>();
		return links;
	}

	/**
	 * @param links the links to set
	 */
	public void setLinks(ArrayList<Link> links) {
		this.links = links;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(mail);
		dest.writeString(role);
		dest.writeString(phone);
		dest.writeString(photo);
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};
	
	public void addLink(final String type, final String url){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String apiUrl = Constants.BASE_API_URL + "/user/addLink";
				try {
					String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);
					JSONObject json = new HttpData(apiUrl).header(Api.APP_AUTH_TOKEN, token)
											.data("id", String.valueOf(id))
											.data("type", type)
											.data("url", url)
											.post().asJSONObject();
					Log.v(TAG, json.toString());					
					if(json.getBoolean("success"))
					{
						JSONObject result = json.getJSONObject("result");
						links.add(new Link(result.getInt("id"), TypeEnum.fromString(type), url));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 catch (HttpDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	//fonction qui fait la requ�te submit
	private boolean save(){

		boolean success = false;
		try {
			//requ�te 
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
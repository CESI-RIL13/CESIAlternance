package fr.cesi.alternance.user;

import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;
import android.os.Parcel;
import android.os.Parcelable;
import android.accounts.AuthenticatorException;
import android.os.Bundle;
import android.util.Log;
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
	private ArrayList<Link> links;
	private long id_promo;
	private boolean isSelected;

	public User() {
		links = new ArrayList<Link>();
		isSelected = false;
	}

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
	
	public static User fromBundle(Bundle bundle){
		User u = new User();
		Log.v(TAG,bundle.toString());
		u.id= Integer.parseInt(bundle.getString(Api.UserColumns.ID));
		u.name=bundle.getString(Api.UserColumns.NAME);
		u.mail=bundle.getString(Api.UserColumns.EMAIL);
		u.role=bundle.getString(Api.UserColumns.ROLE);
		u.phone=bundle.getString(Api.UserColumns.PHONE);
		u.picture_path=bundle.getString(Api.UserColumns.PICTURE_PATH);
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
	
	public boolean getIs_selected() {
		return isSelected;
	}

	public void setIs_selected(Boolean isSelected) {
		this.isSelected = isSelected;
	}

	/**
	 * @return the photo
	 */
	public String getPicture_Path() {
		return picture_path;
	}

	/**
	 * @param photo the photo to set
	 */
	public void setPicture_Path(String photo) {
		this.picture_path = photo;
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
	
	public static interface SaveLinkListener{
		public void onSaveLink();
	}
	
	public void saveLink(final Link link, final SaveLinkListener listener){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String apiUrl = Constants.BASE_API_URL + "/user/saveLink";
				if(link.getId() >0) apiUrl+="/" + link.getId();
				try {
					String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);
					JSONObject json = new HttpData(apiUrl).header(Api.APP_AUTH_TOKEN, token)
											.data("user_id", String.valueOf(id))
											.data("type", link.getType().value())
											.data("url", link.getUrl())
											.post().asJSONObject();
					Log.v(TAG, json.toString());					
					if(json.getBoolean("success"))
					{
						JSONObject result = json.getJSONObject("result");
						if(link.getId()==0){
							link.setId(result.getInt("id"));
							links.add(link);
						}
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
				} finally{
					listener.onSaveLink();
				}
			}
		}).start();
	}

	public boolean save() throws EntityException {

		boolean success = false;
		
		try {
			String url = Constants.BASE_API_URL + "/user/" + (this.id > 0 ? this.id +"/save" : "/save");
			String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);

			HttpData post = new HttpData(url).header(Api.APP_AUTH_TOKEN,token)
					.data("id",""+id)
					.data("name",name)
					.data("email",mail)
					.data("role",role)
					.data("phone",phone)
					.data("id_promo", "" + id_promo)
					.post();
			Log.v("user", ""+id);
			Log.v("USER", post.asString());
			
			JSONObject obj = post.asJSONObject();
			success = obj.getBoolean("success");
			
			if(success) {
				setId(obj.getJSONObject("result").getLong("id"));
			} else {
				throw new EntityException(obj.getString("error"));
			}

		} catch (HttpDataException hde) {
			// TODO Auto-generated catch block
			hde.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}
	
	public boolean delete() throws EntityException{
		boolean success = false;
		
		try {
			String url = Constants.BASE_API_URL + "/user/delete/"+this.getId();
			String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);
			
			HttpData delete = new HttpData(url).header(Api.APP_AUTH_TOKEN,token);
			
			delete.delete();
			
			JSONObject obj = delete.asJSONObject();
			success = obj.getBoolean("success");
			
			if(success) {
				setId(obj.getJSONObject("result").getLong("id"));
			} else {
				throw new EntityException(obj.getString("error"));
			}

		} catch(HttpDataException hde) {
			hde.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return success;
	}
	
	@Override
	public String toString() {
		return "User ["+id+"] " + name;
	}
}
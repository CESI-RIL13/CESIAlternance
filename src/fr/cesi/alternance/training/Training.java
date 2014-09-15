package fr.cesi.alternance.training;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import fr.cesi.alternance.helpers.Entity;
public class Training extends Entity implements Parcelable {
	private long id;
	private String name;
	private String alias;
	private int duration;
	
	public Training(){}
	
	public Training(long id) {
		this.id = id;
	}
	
	public Training(Parcel in){
		id = in.readLong();
		name = in.readString();
		alias = in.readString();
		duration = in.readInt();
	}

	
	public Training fromJSON(JSONObject json) {
		try {
			id = json.getLong("id");
			name = json.getString("name");
			alias = json.getString("alias");
			duration = Integer.parseInt(json.getString("duration"));			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String title) {
		this.name = name;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public JSONObject asJSON() {
		return null;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public String getApiPath() {
		String api_path = "training/" + this.id;
		return api_path;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(alias);
		dest.writeInt(duration);
	}

	public static final Parcelable.Creator<Training> CREATOR = new Parcelable.Creator<Training>() {
		public Training createFromParcel(Parcel in) {
			return new Training(in);
		}

		public Training[] newArray(int size) {
			return new Training[size];
		}
	};
}


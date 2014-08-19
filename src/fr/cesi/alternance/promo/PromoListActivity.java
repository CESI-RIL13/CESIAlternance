package fr.cesi.alternance.promo;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.HomeActivity;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class PromoListActivity extends ListActivity{

	private String role;
	private int training;
	public static final String TAG = "PromoListActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		

		training = getIntent().getExtras().getInt("training");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		TextView name = (TextView) findViewById(R.id.name);
		name.setText(getIntent().getExtras().getString("name"));

		syncPromo();
	}
	
	private void syncPromo(){
		new Thread(new Runnable() {
			
			public void run() {
				try {
					final ArrayList<Promo> listPromo = new ArrayList<Promo>();
					String token = AccountHelper.getData(Api.UserColumns.TOKEN);
					
					HttpData get = new  HttpData(Constants.BASE_API_URL + "/promo").header(Api.APP_AUTH_TOKEN, token).data("training", String.valueOf(training)).get();
					JSONObject json = get.asJSONObject();
					
					if(json.getBoolean("success")) {
						JSONArray result = json.getJSONArray("result");
						for (int i = 0; i < result.length(); i++) {
							Promo p = new Promo();
							p.fromJSON(result.getJSONObject(i));
							listPromo.add(p);
						}
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								getListPromo(listPromo);
								
							}
						});
					}
					
				} catch (HttpDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();

	}
	
	private void getListPromo(final ArrayList<Promo> listPromo) {
		
		PromoAdapter adapter= new PromoAdapter(this, android.R.layout.simple_list_item_2, listPromo);
		
		setListAdapter(adapter);
		
		ListView lv = getListView();
		
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> list, View view, int position,
					long id) {
				
				Intent intent = new Intent(PromoListActivity.this, PromoActivity.class);
				intent.putExtra("id_promo", listPromo.get(position).getId());
				intent.putExtra("name_promo", listPromo.get(position).getName());
				startActivity(intent);
			}
		});
	}


	private class PromoAdapter extends ArrayAdapter<Promo> {

		private Context mContext;
		private int mResource;
		private List<Promo> mItems;
		
		public PromoAdapter(Context context, int resource,
				List<Promo> objects) {
			super(context, resource, objects);	
			
			mContext = context;
			mResource = resource;
			mItems = objects;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) 
				view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
			
			Promo promo = mItems.get(position);
			//ImageView iv = (ImageView) view.findViewById(R.id.img);
			TextView tv = (TextView) view.findViewById(android.R.id.text1);
			//iv.setImageResource(android.R.drawable.btn_default);
			tv.setText(String.valueOf(promo.getName()));
			tv = (TextView) view.findViewById(android.R.id.text2);
			tv.setText(promo.getCode());
			
			return view;
		}
		
	}
	
}

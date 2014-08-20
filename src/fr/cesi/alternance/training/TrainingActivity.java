package fr.cesi.alternance.training;

import fr.cesi.alternance.R;
import fr.cesi.alternance.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.promo.PromoListActivity;
import fr.cesi.alternance.user.UserActivity;
import fr.cesi.alternance.user.UserListActivity;
import android.R.integer;
import android.accounts.AuthenticatorException;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;

public class TrainingActivity extends ListActivity {
	
	public static final String TAG = "TrainingActivity";
	public static final String ROLE = "IF";
    private ProgressBar loader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
				
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		TextView name = (TextView) findViewById(R.id.name);
        loader = (ProgressBar) findViewById(android.R.id.progress);
		
        name.setText("Formations");
		AccountHelper.setContext(this);
		
		//trainingListComplex();
		trainingGetAll();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//return super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.training_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		//return super.onOptionsItemSelected(item);
		switch (item.getItemId()){
        	case R.id.training_menu_document:
        		Toast.makeText(TrainingActivity.this, "Ohhh yeah", Toast.LENGTH_SHORT).show();
                return true;
        	default:
        		return true;
		}
	}
	
	private void trainingGetAll(){

    	getListView().setVisibility(View.GONE);
    	loader.setVisibility(View.VISIBLE);
   
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					//String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);
					String token = AccountHelper.getData(Api.UserColumns.TOKEN);
										
					if (token != null){
						
						String url = Constants.BASE_API_URL + "/training";
						
						//TODO : créer une instance httpdata méthode get
						JSONObject trainings = new HttpData(url).header(Api.APP_AUTH_TOKEN, token).get().asJSONObject();
												
						if(trainings != null){
							trainingListComplex(trainings);
						}

					}
				} catch (HttpDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
		
		
	}
	
	private void trainingListComplex(JSONObject trainings){
		//Log.v(TAG, trainings.toString());
		try {
			if(trainings.getBoolean("success") == true){
				//Log.v(TAG, trainings.get("success").toString());
				//Log.v(TAG, trainings.get("result").toString());
				final ArrayList<Training> training_list = new ArrayList<Training>();
				JSONArray jsonResult = trainings.getJSONArray("result");
				for (int i = 0; i < jsonResult.length(); i++) {
					//Log.v(TAG,jsonResult.get(i).toString());
					JSONObject training = (JSONObject) jsonResult.get(i);
					//Log.v(TAG, training.toString());
					Training p = new Training(Integer.parseInt(training.getString("id")), training.getString("name"), training.getString("alias"), Integer.parseInt(training.getString("duration")));
					training_list.add(p);
				}
				//Log.v(TAG, training_list.toString());
				
				final TrainingAdapter adapter = new TrainingAdapter(this,
						android.R.layout.simple_list_item_2,
						training_list);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						setListAdapter(adapter);
				    	getListView().setVisibility(View.VISIBLE);
				    	loader.setVisibility(View.GONE);
					}
				});

				ListView lv = getListView();
				
				lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> list, View view, int position, long id) {
						// TODO Auto-generated method stub
						/*Toast.makeText(TrainingActivity.this, 
								"Id / "+training_list.get(position).getId(),
								Toast.LENGTH_LONG).show();
						*/
						Intent intent = new Intent(TrainingActivity.this, PromoListActivity.class);
						intent.putExtra("training", training_list.get(position).getId());
						intent.putExtra("name", training_list.get(position).getName());
						//Log.v(TAG, training_list.get(position).getName());
						startActivity(intent);
					}
				});
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class TrainingAdapter extends ArrayAdapter<Training>{
		
		private Context mContext;
		private int mResource;
		private List<Training> mItems;

		public TrainingAdapter(Context context, int resource, List<Training> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			mContext = context;
			mResource = resource;
			mItems = objects;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) 
				view = LayoutInflater.from(mContext).inflate(mResource,parent,false);
			
			Training training = mItems.get(position);

			TextView tv = (TextView) view.findViewById(android.R.id.text1);
			tv.setText(training.getAlias());
			
			TextView tv2 = (TextView) view.findViewById(android.R.id.text2);
			tv2.setText(training.getName());
			
			//tv = (TextView) view.findViewById(R.id.info);
			//tv.setText(product.getPrice() + "€");
			
			return view;
		}
	}
	
}
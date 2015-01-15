package fr.cesi.alternance.training;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import android.accounts.AuthenticatorException;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.helpers.Entity;
import fr.cesi.alternance.helpers.EntitySelector;

public class TrainingEditActivity extends FragmentActivity {
	
	public static final String TAG = "TrainingEditActivity";

	private EditText name;
	private EditText alias;
	private EditText duration;
	private Training training;
	private String mRoleAccount;
	private Button btBrowse;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRoleAccount = AccountHelper.getRole();
		
		setContentView(R.layout.activity_training_edit);
		//initialise les EditText
		name = (EditText)findViewById(R.id.training_edit_name);
		alias = (EditText)findViewById(R.id.training_alias);
		duration = (EditText)findViewById(R.id.training_duration);
		btBrowse = (Button) findViewById(R.id.browse);
		btBrowse.setOnClickListener(mBrowseListener);

		if(getIntent().getExtras() != null){
			training = getIntent().getExtras().getParcelable("training");
		}

		//si le user est pass� charge les champs
		if(training != null){
            name.setText(training.getName());
			alias.setText(training.getAlias());
            if(training.getDuration() != 0)
			    duration.setText(Integer.toString(training.getDuration()));
		}
		setTitle(R.string.training_name);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.save_action).setVisible("IF".equals(mRoleAccount));
		menu.findItem(R.id.delete_action).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_action, menu); 
		return true; 
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
        	case R.id.save_action:

        		new Thread(new Runnable() {
        			@Override
        			public void run() {
        				try {
        					String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);
        					if (token != null){
                                String url;
                                if(training.getId() == 0)
        						    url = Constants.BASE_API_URL + "/training/save";
                                else
                                    url = Constants.BASE_API_URL + "/training/"+training.getId()+"/save";
        						JSONObject json = new HttpData(url).header(Api.APP_AUTH_TOKEN, token)
        								.data("name", name.getText().toString())
										.data("alias", alias.getText().toString())
										.data("duration", duration.getText().toString())
										.post().asJSONObject();
        						Log.v("TrainingEdit",json.toString());
        						if(json.getBoolean("success")) {
        							finish();
        						}
        					}
        				} catch (HttpDataException e) {
        					e.printStackTrace();
        				} catch (AuthenticatorException e) {
        					e.printStackTrace();
        				} catch (IOException e) {
        					e.printStackTrace();
        				} catch (JSONException e) {
							e.printStackTrace();
						}

        			}
        		}).start();
                return true;
        	default:
        		return true;
		}
	}

	private View.OnClickListener mBrowseListener = new View.OnClickListener() {
		public void onClick(View v) {
			loadTraining();
		}
	};

	private void loadTraining() {
		final ProgressDialog progress = ProgressDialog.show(TrainingEditActivity.this, "Load", "In Progress...");
		new Thread(new Runnable() {

			@Override
			public void run() {
				Bundle args = new Bundle();
				ArrayList<Training> list = getListTraining();
				args.putParcelableArrayList("list", list);
				DialogFragment dialog = EntitySelector.newInstance(args, mListener);
				dialog.show(getSupportFragmentManager(), "dialog");  
				runOnUiThread(new Runnable() { 
					@Override
					public void run() { 
						progress.dismiss();
					} 
				}); 
			}
		}).start(); 
	}

	private EntitySelector.SelectUserListener mListener = new EntitySelector.SelectUserListener() {
		@Override
		public void onSelect(Entity selected) {
			training = (Training) selected;
			name.setText(training.getName());
			alias.setText(training.getAlias());
			duration.setText(String.valueOf(training.getDuration()));
		}
	};
	
	public ArrayList<Training> getListTraining() {
		ArrayList<Training> list = new ArrayList<Training>();
		String url = Constants.BASE_API_URL + "/training/load";
		try {
			String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);
			JSONObject json = new HttpData(url).header(Api.APP_AUTH_TOKEN, token).data("role", "IF").get().asJSONObject();
			if(json.getBoolean("success")) {
				JSONArray result = json.getJSONArray("result");
				for (int i = 0; i < result.length(); i++) {
					Training e = new Training().fromJSON(result.getJSONObject(i));
					list.add(e);
				}
			}
		} catch (HttpDataException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

}


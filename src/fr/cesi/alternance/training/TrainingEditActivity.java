package fr.cesi.alternance.training;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import android.accounts.AuthenticatorException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;

public class TrainingEditActivity extends Activity {

	private EditText name;
	private EditText alias;
	private EditText duration;
	private Training training;
	private String mRoleAccount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRoleAccount = AccountHelper.getRole();
		
		setContentView(R.layout.activity_training_edit);
		//initialise les EditText
		name = (EditText)findViewById(R.id.training_edit_name);
		alias = (EditText)findViewById(R.id.training_alias);
		duration = (EditText)findViewById(R.id.training_duration);

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
		//menu.findItem(R.id.delete_action).setVisible("IF".equals(mRoleAccount));
		//menu.findItem(R.id.cancel_action).setVisible("IF".equals(mRoleAccount));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_action, menu); 
		return true; 
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		//return super.onOptionsItemSelected(item);
		switch (item.getItemId()){
        	case R.id.save_action:

        		new Thread(new Runnable() {
        			
        			Intent intent;

        			@Override
        			public void run() {
        				try {
        					String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);
        					if (token != null){
                                String url = "";
                                if(training.getId() == 0)
        						    url = Constants.BASE_API_URL + "/training/save";
                                else
                                    url = Constants.BASE_API_URL + "/training/"+training.getId()+"/save";
        						//TODO : cr�er une instance httpdata m�thode post
        						JSONObject json = new HttpData(url).header(Api.APP_AUTH_TOKEN, token)
        								.data("name", name.getText().toString())
										.data("alias", alias.getText().toString())
										.data("duration", duration.getText().toString())
										.post().asJSONObject();

        						Log.v("TrainingEdit",json.toString());
        						if(json.getBoolean("success"))
        						{
        							finish();
        						}

        					}
        				} catch (HttpDataException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} catch (AuthenticatorException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

        			}
        		}).start();
                return true;
        	default:
        		return true;
		}
	}

}


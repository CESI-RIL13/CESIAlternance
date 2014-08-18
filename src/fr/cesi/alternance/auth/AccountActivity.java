package fr.cesi.alternance.auth;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;
import com.kolapsis.utils.StringUtils;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.HomeActivity;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends AccountAuthenticatorActivity {
	
	public static final String TAG = Constants.APP_NAME + ".AccountActivity";

	private Context mContext;
	
	private AccountManager mAccountManager;
	
	private String mUsername;
	
	private Boolean mRequestNewAccount, mConfirmCredentials;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		
		mContext = this;
		mAccountManager = AccountManager.get(mContext);
		
		final Intent intent = getIntent();
		mUsername = intent.getStringExtra(Authenticator.PARAM_USERNAME);
		mRequestNewAccount = mUsername == null;
		mConfirmCredentials = intent.getBooleanExtra(Authenticator.PARAM_CONFIRM_CREDENTIALS, false);
		
		if(Constants.DEBUG){
			((EditText) findViewById(R.id.email)).setText("eleve1@via-cesi.fr");
			((EditText) findViewById(R.id.pwd)).setText("eleve1");
		}
		
		if(!mRequestNewAccount)
		{
			EditText email = (EditText) findViewById(R.id.email);
			email.setText(mUsername);
			email.setEnabled(false);
		}		
		
		Button submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(mListener);
	}
	
	private View.OnClickListener mListener = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			final ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
			progress.setVisibility(View.VISIBLE);
			String email = mRequestNewAccount ? ((EditText) findViewById(R.id.email)).getText().toString() : mUsername;
			String pwd = ((EditText) findViewById(R.id.pwd)).getText().toString();
			final Bundle data = new Bundle();
			data.putString(Api.UserColumns.EMAIL, email);
			data.putString(Api.UserColumns.PASSWORD, pwd);
			new Thread(new Runnable() {
				@Override
				public void run() {	
					String token = "";
					try {
						token = Api.getInstance().authentificate(data);
						Log.v(TAG, token);
						data.putString(Api.UserColumns.TOKEN, token);
						Log.v(TAG, data.toString());
					} catch (AuthenticatorException e) {
						e.printStackTrace();
						
						showError(e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}	
					
					if (token != "") {
						Bundle user;
						try {
							user = Api.getInstance().loadUser(token);

							if (!user.isEmpty())
								data.putAll(user);

							Log.v(TAG, data.toString());

							if (mConfirmCredentials) {
								finishConfirmCredentials(data);
							} else {
								finishLogin(data);
							}

						} catch (AuthenticatorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							progress.setVisibility(View.INVISIBLE);
						}
					});
				}
			}).start();
		}
	};
	
	private void goNext()
	{
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void showError(final String error)
	{
		final Context context = this;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				
				builder.setTitle("Erreur");
				builder.setMessage(error);
				builder.setPositiveButton("OK", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.cancel();
					}
				});
				
				builder.create().show();
				//Toast.makeText(AccountActivity.this, error, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void finishConfirmCredentials(final Bundle user) {
		if(Constants.DEBUG) {
			Log.i(TAG, "finishConfirmCredentials()");
			Log.i(TAG, "->user: " + user);
		}
		
		final Account account = AccountHelper.getAccountByTypeAndName(Constants.ACCOUNT_TYPE, user.getString(Api.UserColumns.NAME));
		mAccountManager.setPassword(account, user.getString(Api.UserColumns.PASSWORD));
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, true);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private void finishLogin(final Bundle user){
		final String name = user.getString(Api.UserColumns.NAME);
		final String token = user.getString(Api.UserColumns.TOKEN);
		final String pwd = user.getString(Api.UserColumns.PASSWORD);
		user.remove(Api.UserColumns.PASSWORD);
		
		if(Constants.DEBUG){
			Log.i(TAG, "finishLogin()");
			Log.i(TAG, "-> user: " + user);
		}
		
		final Account account = new Account(name, Constants.ACCOUNT_TYPE);
		
		if(!mConfirmCredentials){
			mAccountManager.addAccountExplicitly(account, pwd, user);
			mAccountManager.setAuthToken(account, Constants.ACCOUNT_TOKEN_TYPE, token);
			
			ContentResolver.setSyncAutomatically(account, Constants.PROVIDER_CALENDAR, true);
			//ContentResolver.setSyncAutomatically(account, ProductsProvider.AUTHORITY, true);
			
			long delay = 2628000;
			Bundle extras = new Bundle();
			extras.putLong("periodic_sync", delay);
			
			ContentResolver.addPeriodicSync(account, Constants.PROVIDER_CALENDAR, extras, delay);
			//ContentResolver.addPeriodicSync(account, ProductsProvider.AUTHORITY, extras, delay);
		} else {
			mAccountManager.setPassword(account, pwd);
		}
		
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, name);
		intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);
		intent.putExtra(AccountManager.KEY_PASSWORD, pwd);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}
}

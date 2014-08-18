package fr.cesi.alternance.user;


import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.R;
import fr.cesi.alternance.R.id;
import fr.cesi.alternance.R.layout;
import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.user.User;
import android.R.menu;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;

public class UserActivity extends Activity {

	public static final String TAG = "USER ACTIVITY ==========>>>>";
	private EditText mName;
	private EditText mMail;
	private EditText mPhone;
	private EditText mPwd;
	private String mRoleAccount;//(role de l'utilisateur du téléphone)
	private String mRole;
	private Long mPromo;
	private User mUser;
	private Button btCall;
	private Button btMail;
	private Button btNote;
	private MenuItem mSave;
	private MenuItem mDelete;
	private MenuItem mNoter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);

		//initialise les boutons
		btCall = (Button) findViewById(R.id.callUser);
		btCall.setOnClickListener(mCallListener);
		btMail = (Button) findViewById(R.id.mailerUser);
		btMail.setOnClickListener(mMailerListener);
		btNote = (Button) findViewById(R.id.noteUser);
		btNote.setOnClickListener(mNoteListener);


		//initialise les texteview
		mName = (EditText)findViewById(R.id.nameUser);
		mMail = (EditText)findViewById(R.id.mailUser);
		mPwd = (EditText)findViewById(R.id.pwdUser);
		mPhone = (EditText)findViewById(R.id.phoneUser);

		// récupère le choix du chemin de Home (Eleve ou Intervenant) && l'objet User
		if(getIntent().getExtras() != null){
			mUser = (User)getIntent().getExtras().getParcelable("user");
			mPromo = getIntent().getExtras().getLong("promo_id");
		}


		//si le user est passé charge les champs
		if(mUser != null){
			mName.setText(mUser.getName());
			mPhone.setText(mUser.getPhone());
			mMail.setText(mUser.getMail());
		}

		// récupère le role de l'utilisateur du téléphone
		mRoleAccount = AccountHelper.getRole();

		setTitle(mUser.getRole());

		Log.v(TAG, mUser.getName());
		Log.v(TAG, mUser.getPhone());
		Log.v(TAG, mUser.getMail());

		initialize();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.user, menu); 
		return true; 
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.user_settings_save).setVisible("IF".equals(mRoleAccount));
		menu.findItem(R.id.user_settings_delete).setVisible("IF".equals(mRoleAccount) && mUser.getId() > 0);
		menu.findItem(R.id.user_settings_note).setVisible("Intervenant".equals(mRoleAccount));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.user_settings_save:
			// Comportement du bouton save
			// appelle la fonction qui fait la requète
			if(mUser.getId() == 0) {
				saveUser();
			} else if(mUser.getId() > 0) {
				update();
			}
			return true;
		case R.id.user_settings_delete:
			// Comportement du bouton delete
			deleteUser(mUser.getId());
			return true;
		case R.id.user_settings_note:
			// Comportement du bouton note
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initialize(){

		//GESTION DE L'INTERFACE
		// 1 - l'IF a cliquer sur ajouter user
		if("IF".equals(mRoleAccount) ){

			btCall.setVisibility((mUser.getId() > 0 ? View.VISIBLE : View.GONE));
			btMail.setVisibility((mUser.getId() > 0 ? View.VISIBLE : View.GONE));
			btNote.setVisibility((mUser.getId() > 0 ? View.VISIBLE : View.GONE));
			mPwd.setVisibility((mUser.getId() == 0 ? View.VISIBLE : View.GONE));
		}		
		// 4 - l'eleve(pour plus tard)
		else if("stagiaire".equals(mRoleAccount)){
			//prepa interface
			btNote.setVisibility((mUser.getId() == AccountHelper.getUserId() ? View.VISIBLE : View.GONE));
		}

		//mName.setEnabled(mUser.getId() > 0);
	}

	//listener Bouton
	private View.OnClickListener mCallListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			//Call
			Intent callIntent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+mUser.getPhone()));
			startActivity(callIntent);
		}

	};

	private View.OnClickListener mMailerListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			//Mail
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ mUser.getMail() });
			startActivity(Intent.createChooser(emailIntent, "Envoyer un mail..."));
		}

	};
	private View.OnClickListener mNoteListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			//Show note
		}

	};

	//fonction qui fait la requète submit
	private void saveUser(){

		//modifi l'objet user courant avec les nouveaux paramètres
		mUser.setName(mName.getText().toString());
		mUser.setMail(mMail.getText().toString());
		mUser.setPhone(mPhone.getText().toString());

		//regarde si un champ est vide
		if(mUser.getMail().isEmpty()||mUser.getName().isEmpty()||mUser.getPhone().isEmpty()){
			new AlertDialog.Builder(this).setTitle("Erreur").setMessage("A field is empty !").create().show();
		}

		//écran d'attente
		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Submit", "In Progress...");

		//déclare un thread qui fait la requète
		new Thread(new Runnable() {
			@Override
			public void run() {

				if(mUser.save()) {
					ActivityCompat.invalidateOptionsMenu(UserActivity.this);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							UserActivity.this.initialize();
							progress.dismiss();
						}
					});
				}

			}
		}).start();
	}

	private void update() {

		//modifi l'objet user courant avec les nouveaux paramètres
		mUser.setMail(mMail.getText().toString());
		mUser.setPhone(mPhone.getText().toString());

		//regarde si un champ est vide
		if(mUser.getMail().isEmpty() || mUser.getPhone().isEmpty()){
			new AlertDialog.Builder(this).setTitle("Erreur").setMessage("A field is empty !").create().show();
		}

		//écran d'attente
		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Submit", "In Progress...");

		//déclare un thread qui fait la requète
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(mUser.save()) {
					ActivityCompat.invalidateOptionsMenu(UserActivity.this);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							UserActivity.this.initialize();
							progress.dismiss();
						}
					});
				}
			}
		}).start();
	}
	//fonction qui delete un utilisateur
	private String deleteUser(long l){
		String error ;
		String success;
		//écran d'attente
		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Delete", "In Progress...");

		//déclare un thread qui fait la requète
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
					progress.dismiss();
					//requète 
					//requète 
					//appel de la fonction addUser
					String url = Constants.BASE_API_URL + "/user/delete/"+mUser.getId();
					Log.v(TAG,url);
					//Authentification
					String token = AccountHelper.getData(Api.UserColumns.TOKEN);
					HttpData delete = new HttpData(url).header(Api.APP_AUTH_TOKEN,token);
					delete.delete();
					Log.d("EEE", delete.asString());
					JSONObject obj = delete.asJSONObject();
					if(obj.getBoolean("success")) {
						UserActivity.this.finish();
					}
				} catch (HttpDataException hde) {
					// TODO Auto-generated catch block
					hde.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
		success = "successfully submitted ! ";
		return success;
	}
	//fonction pour aller sur l'onglet qui permet d'afficher et rentrer des notes
	private void noteUser(){

	}
}

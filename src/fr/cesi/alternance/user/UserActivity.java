package fr.cesi.alternance.user;


import android.app.DialogFragment;
import android.widget.ImageView;
import fr.cesi.alternance.user.PhotoUserDialog;
import org.json.JSONException;
import org.json.JSONObject;
import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;
import com.kolapsis.utils.DownloadImageTask;
import fr.cesi.alternance.R;
import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.user.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UserActivity extends Activity {

	public static final String TAG = "USER ACTIVITY ==========>>>>";
	private TextView mName,mMail,mPhone;
	private String mRoleAccount,mPicture_path;
	private Long mPromo;
	private User mUser;
	private Button btCall,btMail,btNote;
    private ImageView mPicture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRoleAccount = AccountHelper.getRole();
		
		if("IF".equals(mRoleAccount)) {
			setContentView(R.layout.activity_user_edit);
			//initialise les EditText
			mName = (TextView)findViewById(R.id.nameUser);
			mMail = (EditText)findViewById(R.id.mailUser);
			mPhone = (EditText)findViewById(R.id.phoneUser);
		} else {
			setContentView(R.layout.activity_user);
			//initialise les TextView
			mName = (TextView)findViewById(R.id.nameUser);
			mMail = (TextView)findViewById(R.id.mailUser);
			mPhone = (TextView)findViewById(R.id.phoneUser);			
		}

		//initialise les boutons
		btCall = (Button) findViewById(R.id.callUser);
		btCall.setOnClickListener(mCallListener);
		btMail = (Button) findViewById(R.id.mailerUser);
		btMail.setOnClickListener(mMailerListener);
		btNote = (Button) findViewById(R.id.noteUser);
		btNote.setOnClickListener(mNoteListener);

        //initialise l'image
        mPicture = (ImageView)findViewById(R.id.picture);

		if(getIntent().getExtras() != null){
			mUser = (User)getIntent().getExtras().getParcelable("user");
			mPromo = getIntent().getExtras().getLong("id_promo");
		}

		//si le user est pass� charge les champs
		if(mUser != null){
            mPicture.setTag("http://cesi.kolapsis.com/cesi_alternance/picture/"+mUser.getPicture_path());
            new DownloadImageTask(mPicture).execute();
            mName.setText(mUser.getName());
			mPhone.setText(mUser.getPhone());
			mMail.setText(mUser.getMail());
		}

		setTitle(mUser.getRole());

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
        menu.findItem(R.id.user_settings_photo).setVisible("IF".equals(mRoleAccount));
		menu.findItem(R.id.user_settings_delete).setVisible("IF".equals(mRoleAccount) && mUser.getId() > 0);
		menu.findItem(R.id.user_settings_note).setVisible("Intervenant".equals(mRoleAccount));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.user_settings_save:
			save(mUser.getId() == 0);
			return true;
		case R.id.user_settings_delete:
			deleteUser(mUser.getId());
			return true;
		case R.id.user_settings_note:
			// Comportement du bouton note
			return true;
        case R.id.user_settings_photo:
            DialogFragment dialog = new PhotoUserDialog(mUser,mUploadListener);
            dialog.show(getFragmentManager(), "dialog");
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    private PhotoUserDialog.UploadListener mUploadListener = new PhotoUserDialog.UploadListener() {
        @Override
        public void onUpload(String path) {
            mPicture.setTag("http://cesi.kolapsis.com/cesi_alternance/picture/"+path);
            new DownloadImageTask(mPicture).execute();
        }
    };

	private void initialize(){

		//GESTION DE L'INTERFACE
		// 1 - l'IF a cliquer sur ajouter user
		if("IF".equals(mRoleAccount) ){

			btCall.setVisibility((mUser.getId() > 0 ? View.VISIBLE : View.GONE));
			btMail.setVisibility((mUser.getId() > 0 ? View.VISIBLE : View.GONE));
			btNote.setVisibility((mUser.getId() > 0 ? View.VISIBLE : View.GONE));
		}		
		// 4 - l'eleve(pour plus tard)
		else if("stagiaire".equals(mRoleAccount)){
			//prepa interface
			btNote.setVisibility((mUser.getId() == AccountHelper.getUserId() ? View.VISIBLE : View.GONE));
		}

	}

	//listener Bouton
	private View.OnClickListener mCallListener = new View.OnClickListener() {

		public void onClick(View v) {
			//Call
			Intent callIntent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+mUser.getPhone()));
			startActivity(callIntent);
		}

	};

	
	private View.OnClickListener mMailerListener = new View.OnClickListener() {

		public void onClick(View v) {
			//Mail
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ mUser.getMail() });
			startActivity(Intent.createChooser(emailIntent, "Envoyer un mail..."));
		}

	};

	
	private View.OnClickListener mNoteListener = new View.OnClickListener() {

		public void onClick(View v) {
			//Show note
		}

	};

	
	private void save(Boolean add) {
		//modifi l'objet user courant avec les nouveaux param�tres
		if(add) {
			mUser.setName(mName.getText().toString());
			mUser.setId_promo(mPromo);
		}
        mUser.setPicture_path(mPicture_path);
		mUser.setMail(mMail.getText().toString());
		mUser.setPhone(mPhone.getText().toString());

		//regarde si un champ est vide
		if((add && mUser.getName().isEmpty()) || mUser.getMail().isEmpty()|| mUser.getPhone().isEmpty()){
			new AlertDialog.Builder(this).setTitle("Erreur").setMessage("A field is empty !").create().show();
		}

		//�cran d'attente
		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Submit", "In Progress...");

		//d�clare un thread qui fait la requ�te
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
		
		//�cran d'attente
		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Delete", "In Progress...");

		//d�clare un thread qui fait la requ�te
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
					progress.dismiss();

					String url = Constants.BASE_API_URL + "/user/delete/"+mUser.getId();
					String token = AccountHelper.getData(Api.UserColumns.TOKEN);
					
					HttpData delete = new HttpData(url).header(Api.APP_AUTH_TOKEN,token);
					
					delete.delete();
					
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

package fr.cesi.alternance.promo;

import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.DownloadImageTask;
import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.user.User;
import fr.cesi.alternance.user.UserActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PromoEditActivity extends Activity {
	
	private EditText begin;
	private EditText end;
	private EditText number;
	private EditText code;
	private EditText id_planning;
	private String mRoleAccount;
	private Promo promo;
	private Long training;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRoleAccount = AccountHelper.getRole();
		
		setContentView(R.layout.activity_promo_edit);
		//initialise les EditText
		begin = (EditText)findViewById(R.id.nameUser);
		end = (EditText)findViewById(R.id.mailUser);
		number = (EditText)findViewById(R.id.phoneUser);
		code = (EditText)findViewById(R.id.phoneUser);
		id_planning = (EditText)findViewById(R.id.phoneUser);

		if(getIntent().getExtras() != null){
			promo = (Promo)getIntent().getExtras().getParcelable("promo");
			training = getIntent().getExtras().getLong("id_training");
		}

		//si le user est passé charge les champs
		if(promo != null){
            begin.setText(promo.getBegin());
			end.setText(promo.getEnd());
			number.setText(promo.getNumber());
			code.setText(promo.getCode());
			id_planning.setText(promo.getId_planning());
		}

		setTitle("Create new Promo");

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
			save(mUser.getId() == 0);
			return true;
		case R.id.user_settings_delete:
			deleteUser(mUser.getId());
			return true;
		case R.id.user_settings_note:
			// Comportement du bouton note
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void save(Boolean add) {
		//modifi l'objet user courant avec les nouveaux paramï¿½tres
		if(add) {
			mUser.setName(mName.getText().toString());
			mUser.setId_promo(mPromo);
		}

		mUser.setMail(mMail.getText().toString());
		mUser.setPhone(mPhone.getText().toString());

		//regarde si un champ est vide
		if((add && mUser.getName().isEmpty()) || mUser.getMail().isEmpty()|| mUser.getPhone().isEmpty()){
			new AlertDialog.Builder(this).setTitle("Erreur").setMessage("A field is empty !").create().show();
		}

		//écran d'attente
		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Submit", "In Progress...");

		//déclare un thread qui fait la requéte
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
	private String deletePromo(long l){
		String error ;
		String success;
		
		//écran d'attente
		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Delete", "In Progress...");

		//déclare un thread qui fait la requéte
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

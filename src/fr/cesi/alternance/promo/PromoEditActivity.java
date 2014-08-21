package fr.cesi.alternance.promo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.cesi.alternance.R;
import fr.cesi.alternance.helpers.AccountHelper;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

public class PromoEditActivity extends Activity {
	
	private EditText begin;
	private EditText end;
	private EditText number;
	private EditText code;
	private EditText id_planning;
	private String mRoleAccount;
	private Promo promo;
	private Long training;
	private SimpleDateFormat fmt = new SimpleDateFormat("dd MMMM yyyy");


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRoleAccount = AccountHelper.getRole();
		
		setContentView(R.layout.activity_promo_edit);
		//initialise les EditText
		begin = (EditText)findViewById(R.id.begin);
		end = (EditText)findViewById(R.id.end);
		number = (EditText)findViewById(R.id.number);
		code = (EditText)findViewById(R.id.code);
		id_planning = (EditText)findViewById(R.id.id_planning);

		if(getIntent().getExtras() != null){
			promo = (Promo)getIntent().getExtras().getParcelable("promo");
			training = getIntent().getExtras().getLong("id_training");
		}

		//si le user est passé charge les champs
		if(promo != null){
            begin.setText(fmt.format(promo.getBegin()));
			end.setText(fmt.format(promo.getEnd()));
			number.setText(""+promo.getNumber());
			code.setText(promo.getCode());
			id_planning.setText(promo.getId_planning());
		}

		setTitle(R.string.promo_add);
		
		begin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(promo.getBegin().getTime());
				
				new DatePickerDialog(PromoEditActivity.this, beginCallBack, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE)).show();
			}
		});

		end.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(promo.getEnd().getTime());
				
				new DatePickerDialog(PromoEditActivity.this, endCallBack, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE)).show();
			}
		});
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.save_action).setVisible("IF".equals(mRoleAccount));
		menu.findItem(R.id.delete_action).setVisible("IF".equals(mRoleAccount) && promo.getId() > 0);
		menu.findItem(R.id.cancel_action).setVisible("IF".equals(mRoleAccount));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_action, menu); 
		return true; 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.user_settings_save:
//			save(mUser.getId() == 0);
			return true;
		case R.id.user_settings_delete:
//			deleteUser(mUser.getId());
			return true;
		case R.id.user_settings_note:
			// Comportement du bouton note
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private DatePickerDialog.OnDateSetListener beginCallBack = new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, monthOfYear);
			c.set(Calendar.DATE, dayOfMonth);
			
			promo.setBegin(c.getTime());
			
			begin.setText(fmt.format(c.getTime()));
		}
	};	

	private DatePickerDialog.OnDateSetListener endCallBack = new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, monthOfYear);
			c.set(Calendar.DATE, dayOfMonth);
			
			promo.setEnd(c.getTime());
			
			end.setText(fmt.format(c.getTime()));
		}
	};	


//	private void save(Boolean add) {
//		//modifi l'objet user courant avec les nouveaux paramï¿½tres
//		if(add) {
////			mUser.setName(mName.getText().toString());
////			mUser.setId_promo(mPromo);
//		}
//
////		mUser.setMail(mMail.getText().toString());
////		mUser.setPhone(mPhone.getText().toString());
//
//		//regarde si un champ est vide
////		if((add && mUser.getName().isEmpty()) || mUser.getMail().isEmpty()|| mUser.getPhone().isEmpty()){
////			new AlertDialog.Builder(this).setTitle("Erreur").setMessage("A field is empty !").create().show();
////		}
//
//		//écran d'attente
////		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Submit", "In Progress...");
//
//		//déclare un thread qui fait la requéte
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//
////				if(mUser.save()) {
////					ActivityCompat.invalidateOptionsMenu(UserActivity.this);
////					runOnUiThread(new Runnable() {
////						@Override
////						public void run() {
////							UserActivity.this.initialize();
////							progress.dismiss();
////						}
////					});
//				}
//
//			}
//		}).start();		
//	}
	
	//fonction qui delete un utilisateur
//	private String deletePromo(long l){
//		String error ;
//		String success;
//		
//		//écran d'attente
//		final ProgressDialog progress = ProgressDialog.show(UserActivity.this, "Delete", "In Progress...");
//
//		//déclare un thread qui fait la requéte
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(3000);
//					progress.dismiss();
//
//					String url = Constants.BASE_API_URL + "/user/delete/"+mUser.getId();
//					String token = AccountHelper.getData(Api.UserColumns.TOKEN);
//					
//					HttpData delete = new HttpData(url).header(Api.APP_AUTH_TOKEN,token);
//					
//					delete.delete();
//					
//					JSONObject obj = delete.asJSONObject();
//					
//					if(obj.getBoolean("success")) {
//						UserActivity.this.finish();
//					}
//				
//				} catch (HttpDataException hde) {
//					// TODO Auto-generated catch block
//					hde.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			}
//		}).start();
//		success = "successfully submitted ! ";
//		return success;
//	}

}

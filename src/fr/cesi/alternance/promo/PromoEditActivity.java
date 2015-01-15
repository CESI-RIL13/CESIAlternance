package fr.cesi.alternance.promo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import fr.cesi.alternance.R;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.helpers.Entity.EntityException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PromoEditActivity extends Activity {
	
	private TextView begin;
	private TextView end;
	private EditText number;
	private EditText code;
	private EditText id_planning;
	private String mRoleAccount;
	private Promo promo;
	private Long id_training;
	private SimpleDateFormat fmt = new SimpleDateFormat("dd MMMM yyyy");


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mRoleAccount = AccountHelper.getRole();
		
		setContentView(R.layout.activity_promo_edit);
		//initialise les EditText
		begin = (TextView)findViewById(R.id.begin);
		end = (TextView)findViewById(R.id.end);
		number = (EditText)findViewById(R.id.number);
		code = (EditText)findViewById(R.id.code);
		id_planning = (EditText)findViewById(R.id.id_planning);

		if(getIntent().getExtras() != null){
			promo = (Promo)getIntent().getExtras().getParcelable("promo");
			id_training = getIntent().getExtras().getLong("id_training");
		}
		
		//Log.v("PromoEditActivity", "id_training = " + id_training.toString());

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
		case R.id.save_action:
			save();
			return true;
		case R.id.delete_action:
			deletePromo();
			return true;
		case R.id.cancel_action:
			// Comportement du bouton cancel
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


	private void save() {
		
		try {
			promo.setBegin(fmt.parse(begin.getText().toString()));
			promo.setEnd(fmt.parse(end.getText().toString()));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		promo.setNumber(Long.decode(number.getText().toString()));
		promo.setCode(code.getText().toString());
		promo.setId_planning(id_planning.getText().toString());
		
		Log.v("PROMO EDIT", promo.toString());
		
		//regarde si un champ est vide
		if(promo.getNumber() < 1 || promo.getCode().isEmpty()){
			new AlertDialog.Builder(this).setTitle("Erreur").setMessage("A field is empty !").create().show();
		}

		//écran d'attente
		final ProgressDialog progress = ProgressDialog.show(PromoEditActivity.this, "Submit", "In Progress...");

		//déclare un thread qui fait la requéte
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					promo.save(id_training);
				} catch (final EntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(PromoEditActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				} finally {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							progress.dismiss();
							finish();
						}
					});					
				}

			}
		}).start();		
	}
	
	//fonction qui delete un utilisateur
	private void deletePromo(){
		final ProgressDialog progress = ProgressDialog.show(PromoEditActivity.this, "Delete", "In Progress...");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(promo.delete()) {
						ActivityCompat.invalidateOptionsMenu(PromoEditActivity.this);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progress.dismiss();
								finish();
							}
						});				
					}
				} catch (EntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

}

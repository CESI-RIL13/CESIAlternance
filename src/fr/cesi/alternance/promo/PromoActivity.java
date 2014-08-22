package fr.cesi.alternance.promo;
import android.os.Bundle;
import fr.cesi.alternance.R;
import fr.cesi.alternance.docs.DocListActivity;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.user.UserListActivity;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PromoActivity extends ListActivity {
	public static final String TAG = "PromoListActivity";
	private String role;
	private long promo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		promo = getIntent().getExtras().getLong("id_promo");		
		
		TextView name = (TextView) findViewById(R.id.name);
		name.setText(getIntent().getExtras().getString("name_promo"));
		
		listGoTo();
	}

	private void listGoTo() {
		
		final String[] values = {getString(R.string.stagiaire_title), getString(R.string.intervenant_title), getString(R.string.doc_title)/*, getString(R.string.support_title)*/};
		
		ListView lv = getListView();

		lv.setAdapter(new ArrayAdapter<String>(PromoActivity.this,  android.R.layout.simple_list_item_1, values));

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> promo_toGo, View view, int position, long id) {
				
				try {
					Intent intent = new Intent();
					if (position == 0){
						intent.setClass(PromoActivity.this, UserListActivity.class);
						intent.putExtra("users_list_name", "Stagiaires");
						role = "Stagiaire";
					}
					else if (position == 1){
						intent.setClass(PromoActivity.this, UserListActivity.class);
						intent.putExtra("users_list_name", "Intervenants");
						role = "Intervenant";
					}
					else if (position == 2){
						intent.setClass(PromoActivity.this, DocListActivity.class);
						intent.putExtra("users_list_name", "Documents");
					}
//					else if (position == 3){
//						intent.setClass(PromoActivity.this, UserListActivity.class);
//						intent.putExtra("users_list_name", "Supports");
//					}					
					if (role != null)
						intent.putExtra("role", role);
					
					intent.putExtra("id_promo", promo);
					startActivity(intent);
					//finish();
				} catch (Exception e) {
					Toast.makeText(PromoActivity.this, "Erreur lancement activity " + e , Toast.LENGTH_LONG).show();
				}	
					
			}
		});

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_action, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.add_doc_action).setVisible("IF".equals(AccountHelper.getRole()));
		menu.findItem(R.id.add_list_action).setVisible("IF".equals(AccountHelper.getRole()));
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) { 
        switch (item.getItemId()) { 
        case R.id.add_list_action:
        	
//            Promo newPromo = new Promo();
//            newPromo.setName("");
//            newPromo.setNumber(0);
//            newPromo.setCode("");
//            newPromo.setEnd("");
//            newPromo.setBegin("");
//            newPromo.setId_planning("");
//
//            Intent intent = new Intent(PromoActivity.this, PromoEditActivity.class); 
//            intent.putExtra("promo", newPromo); 
//            intent.putExtra("id_training", training); 
//            startActivity(intent); 
            return true; 
        default: 
            return super.onOptionsItemSelected(item); 
        } 
    }
}

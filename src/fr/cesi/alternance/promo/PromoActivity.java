package fr.cesi.alternance.promo;
import android.os.Bundle;
import fr.cesi.alternance.R;
import fr.cesi.alternance.docs.Doc;
import fr.cesi.alternance.docs.DocListActivity;
import fr.cesi.alternance.docs.DocUploadDialog;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.user.UserListActivity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PromoActivity extends FragmentActivity {
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
		
		ListView lv = (ListView) findViewById(android.R.id.list);

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
						intent.putExtra("add", "IF".equals(AccountHelper.getRole()) || "Intervenant".equals(AccountHelper.getRole()));
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
		menu.findItem(R.id.view_doc_action).setVisible(false);
		menu.findItem(R.id.add_list_action).setVisible(false);
		menu.findItem(R.id.add_doc_action).setVisible(!"Stagiaire".equals(AccountHelper.getRole()));
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) { 
        switch (item.getItemId()) { 
        case R.id.add_list_action:
//        	Promo newPromo = new Promo();
//            newPromo.setName("");
//            newPromo.setNumber(Long.valueOf(0));
//            newPromo.setCode("");
//            newPromo.setEnd(new Date());
//            newPromo.setBegin(new Date());
//            newPromo.setId_planning("");
//
//            intent = new Intent(PromoListActivity.this, PromoEditActivity.class); 
//            intent.putExtra("promo", newPromo);
//            intent.putExtra("id_training", id_training); 
//            startActivity(intent); 
            return true;
        case R.id.add_doc_action:
        	Bundle args = new Bundle();
        	args.putLong("id_establishment", 1);
        	args.putLong("id_promo", promo);
			DialogFragment dialog = DocUploadDialog.newInstance(args, mUploadListener);
			dialog.show(getSupportFragmentManager(), "dialog");       	
        	return true;
        case R.id.view_doc_action:
            return true;
        default: 
            return super.onOptionsItemSelected(item); 
        } 
    }
	
private DocUploadDialog.UploadListener mUploadListener = new DocUploadDialog.UploadListener() {
		
		@Override
		public void onUpload(Doc newDoc) {
			
		}
	};
}

package fr.cesi.alternance.promo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;
import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.docs.Doc;
import fr.cesi.alternance.docs.DocListActivity;
import fr.cesi.alternance.docs.DocUploadDialog;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.training.Training;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class PromoListActivity extends FragmentActivity{

	private ListView mListView;
	private Training training;
    private ProgressBar loader;
	
    public static final String TAG = "PromoListActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_home);
		mListView = (ListView) findViewById(android.R.id.list);
        loader = (ProgressBar) findViewById(android.R.id.progress);
		training = getIntent().getExtras().getParcelable("training");

		TextView name = (TextView) findViewById(R.id.name);

		name.setText(training.getName());
		
	}

    @Override
    protected void onResume() {
    	syncPromo();
    	super.onResume();
    }

	private void syncPromo(){

    	mListView.setVisibility(View.GONE);

    	loader.setVisibility(View.VISIBLE);
  
		new Thread(new Runnable() {
			
			public void run() {
				
				try {
					final ArrayList<Promo> listPromo = new ArrayList<Promo>();
					String token = AccountHelper.getData(Api.UserColumns.TOKEN);
					
					HttpData get = new  HttpData(Constants.BASE_API_URL + "/promo").header(Api.APP_AUTH_TOKEN, token).data("id_training", String.valueOf(training.getId())).get();
					JSONObject json = get.asJSONObject();
					
					if(json.getBoolean("success")) {
						JSONArray result = json.getJSONArray("result");
						for (int i = 0; i < result.length(); i++) {
							Promo p = new Promo();
							p.fromJSON(result.getJSONObject(i));
							listPromo.add(p);
						}

						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								getListPromo(listPromo);
								mListView.setVisibility(View.VISIBLE);
						    	loader.setVisibility(View.GONE);	
							}
						});

					}

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							getListPromo(listPromo);
							mListView.setVisibility(View.VISIBLE);
					    	loader.setVisibility(View.GONE);	
						}
					});
					
				} catch (HttpDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();

	}

	public boolean onCreateOptionsMenu (Menu menu) {
    	if(!"IF".equals(AccountHelper.getRole()))
    		return false;
        getMenuInflater().inflate(R.menu.add_action, menu);
        return true;
    } 

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.add_doc_action).setVisible("IF".equals(AccountHelper.getRole()));
		menu.findItem(R.id.add_list_action).setVisible(false);
		menu.findItem(R.id.add_list_action).setVisible("IF".equals(AccountHelper.getRole()));
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
        switch (item.getItemId()) { 
	        case R.id.add_list_action:
	            Promo newPromo = new Promo();
	            newPromo.setName("");
	            newPromo.setNumber(Long.valueOf(0));
	            newPromo.setCode("");
	            newPromo.setEnd(new Date());
	            newPromo.setBegin(new Date());
	            newPromo.setId_planning("");
	
	            intent = new Intent(PromoListActivity.this, PromoEditActivity.class); 
	            intent.putExtra("promo", newPromo);
	            intent.putExtra("id_training", training.getId()); 
	            startActivity(intent); 
	            return true;
	        case R.id.add_doc_action:
	        	Bundle args = new Bundle();
	        	args.putLong("id_establishment", 1);
	        	args.putLong("id_training", training.getId());
				DialogFragment dialog = DocUploadDialog.newInstance(args, mUploadListener);
				dialog.show(getSupportFragmentManager(), "dialog");       	
	        	return true;
	        case R.id.view_doc_action:
	            intent = new Intent(this, DocListActivity.class);
	            intent.putExtra("id_establishment", 1L);
	            intent.putExtra("id_training", training.getId());
	            intent.putExtra("add", true);
	            startActivity(intent);  	
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

	private void getListPromo(final ArrayList<Promo> listPromo) {
		
		PromoAdapter adapter= new PromoAdapter(this, android.R.layout.simple_list_item_2, listPromo);
		
		mListView.setAdapter(adapter);
		
		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> list, View view, int position,
					long id) {

				Intent intent = new Intent(PromoListActivity.this, PromoActivity.class);
				intent.putExtra("promo", (Promo)listPromo.get(position));

				startActivity(intent);
			}


		});
		
		if("IF".equals(AccountHelper.getRole())) {
			mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> list, View view, int position,
						long id) {
	
					Intent intent = new Intent(PromoListActivity.this, PromoEditActivity.class);
					intent.putExtra("promo", (Promo)listPromo.get(position));
					intent.putExtra("id_training", training.getId());
					startActivity(intent);
					return true;
				}
	
			});
		}
	}

	private class PromoAdapter extends ArrayAdapter<Promo> {

		private Context mContext;
		private int mResource;
		private List<Promo> mItems;
		
		public PromoAdapter(Context context, int resource,
				List<Promo> objects) {
			super(context, resource, objects);	
			
			mContext = context;
			mResource = resource;
			mItems = objects;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) 
				view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
			
			Promo promo = mItems.get(position);
			//ImageView iv = (ImageView) view.findViewById(R.id.img);
			TextView tv = (TextView) view.findViewById(android.R.id.text1);
			//iv.setImageResource(android.R.drawable.btn_default);
			tv.setText(String.valueOf(promo.getName()));
			tv = (TextView) view.findViewById(android.R.id.text2);
			tv.setText(promo.getCode());
			
			return view;
		}
		
	}
	
}

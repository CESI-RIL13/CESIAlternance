package fr.cesi.alternance.docs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;

public class DocListActivity extends ListActivity {

	private Boolean selection;
	private DocsAdapter adapter;
	private ArrayList <Doc> docs = new ArrayList<Doc>();
	private boolean remove;
	private static Account account;

	private static final String NAME				= "name";
	private static final String DESCRIPTION         = "description";
	private static final String PATH 				= "path";


	// ---------------------------------------------------------------------------------------
	// Procedures onCreate 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doc_list_activity);
		ListDoc();
		getListView().setOnItemLongClickListener(docLongClick);
		getListView().setOnItemClickListener(docClick);
		selection = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.menu_doc_list, menu);

		MenuItem item =  menu.findItem(R.id.doc_delete);
		item.setVisible(false);
		return true;
	}


	// ---------------------------------------------------------------------------------------
	// Procedures menu

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.doc_delete :
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.progress_delete_title);
			builder.setMessage(R.string.menu_contextuel_mess);
			builder.setNegativeButton(R.string.menu_contextuel_cancel, new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.setPositiveButton(R.string.menu_contextuel_delete, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					remove = true;
				}
			});
			builder.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (remove) {
						List<Doc> rm = new ArrayList<Doc>();
						for(Doc doc : docs)
							if(doc.isSelected()) rm.add(doc);

						for (Doc doc : rm) 
							docs.remove(doc);

						adapter.notifyDataSetChanged();
						afficheMenuDelete();
						invalidateOptionsMenu();
						deleteDocs(rm);
					}
					remove = false;
				}
			});

			builder.create();
			builder.show();

			return true;
		case R.id.doc_add : 
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item =  menu.findItem(R.id.doc_delete);
		if(afficheMenuDelete())
			item.setVisible(true);
		else item.setVisible(false);

		return true;
	}

	// -----------------------------------------------------------------------------------------------------
	// Procedure gestion des clics

	private OnItemClickListener docClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
			if(selection){
				if(checkItem(position))
					view.setBackgroundColor(Color.CYAN);
				else view.setBackgroundColor(Color.WHITE);
				invalidateOptionsMenu();
			}
		}
	};

	private OnItemLongClickListener docLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
			selection = true;

			if(checkItem(position))
				view.setBackgroundColor(Color.CYAN);
			else view.setBackgroundColor(Color.WHITE);

			invalidateOptionsMenu();
			return true;
		}
	};


	// --------------------------------------------------------------------------------------------
	// Fonctions utiles

	private boolean checkItem(int position) {
		Doc doc = ((DocsAdapter) getListAdapter()).getItem(position);
		doc.setSelected(!doc.isSelected());
		docs.get(position).setSelected(doc.isSelected());

		return doc.isSelected();
	}

	private boolean afficheMenuDelete() {
		for(int i = 0; i < docs.size(); i++){
			Doc doc = docs.get(i);
			Log.v("selection", ""+doc.isSelected()+i);
			if(doc.isSelected())
				return true;
		}

		selection = false;
		return false;
	}

	private void traceSel() {
		SparseBooleanArray sel = getListView().getCheckedItemPositions();
		for(int i = 0;i<adapter.getCount();i++){
			Log.v("select" + i, "" +sel.get(i));
		}
	}

	private void deleteDocs(final List<Doc> rm) {
		final ProgressDialog progress = ProgressDialog.show(DocListActivity.this, getString(R.string.progress_delete_title), getString(R.string.progress_delete_infos));

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final String url = Constants.BASE_API_URL +"/document/delete/";
					final String token = AccountHelper.blockingGetAuthToken(account, Constants.ACCOUNT_TOKEN_TYPE, true);

					account = AccountHelper.getAccount();
					for(int i= 0;i<rm.size();i++) {
						Doc d = rm.get(i);
						Log.v("objet doc avant delete", ""+ d.getName());
						String u = url + d.getId();
						Log.v("objet u", ""+ u);
						HttpData get = new HttpData(u).header(Api.APP_AUTH_TOKEN, token).get();
						Log.v("retour du serveur", get.asString());
						JSONObject result = get.asJSONObject();
						Log.v("resultat de l'appel serveur", ""+ result.toString());
						if(result.getBoolean("success")){
							docs.remove(d);
							Log.v("objet docs apres delete", ""+ docs.toString());
						}

					}
				} catch (HttpDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {

					progress.dismiss();
				}
			}
		}).start();
	}

	private boolean connexionServer() throws AuthenticatorException, IOException{
		final ProgressDialog progress = ProgressDialog.show(DocListActivity.this, getString(R.string.progress_chargement), getString(R.string.progress_chargement_infos));
		final String url = Constants.BASE_API_URL +"/document";
		account = AccountHelper.getAccount();
		//final String token = Constants.TOKEN_TEST;
		//final String role = AccountHelper.getData(Api.UserColumns.ROLE);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final String token = AccountHelper.blockingGetAuthToken(account, Constants.ACCOUNT_TOKEN_TYPE, true);
					JSONObject json = new HttpData(url).header(Api.APP_AUTH_TOKEN, token).get().asJSONObject();
					Log.v("objet Json", ""+ json.toString());

					JSONArray result = json.getJSONArray("result");
					Log.v("objet RESULT", ""+ result.toString());
					docs.clear();

					for(int i= 0;i<result.length();i++) {
						JSONObject objet = result.getJSONObject(i);
						Doc d = new Doc().fromJSON(objet);
						docs.add(d);
					}
					generationDeVue();

				} catch (HttpDataException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {

					progress.dismiss();
				}
				
			}

		}).start();

		return true;
	}
	// ------------------------------------------------------------------------------------
	// Corps de l'activity

	private void generationDeVue(){
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				adapter = new DocsAdapter(DocListActivity.this,R.layout.doc_row,docs);
				setListAdapter(adapter);
			}
		});
	}


	private void ListDoc() {

		try {
			connexionServer();

		} catch (AuthenticatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// -------------------------------------------------------------------------------------
	// Class adapter view

	private class DocsAdapter extends ArrayAdapter<Doc>{

		private Context mContext;
		private int mRessource;
		private List<Doc> mItems;

		public DocsAdapter(Context context, int resource,
				List<Doc> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			mContext = context;
			mRessource = resource;
			mItems = objects;
		}
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (view == null) view = LayoutInflater.from(mContext).inflate(mRessource, parent, false);

			Doc doc = mItems.get(position);
			TextView tv = (TextView) view.findViewById(R.id.titre);
			tv.setText(doc.getName());
			tv = (TextView) view.findViewById(R.id.desc);
			tv.setText(doc.getDescription());

			if(doc.isSelected())
				view.setBackgroundColor(Color.CYAN);
			else view.setBackgroundColor(Color.WHITE);

			return view;
		}

		@Override
		public Doc getItem(int position) {
			return mItems.get(position);
		}
	}

}

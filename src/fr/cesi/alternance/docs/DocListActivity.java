package fr.cesi.alternance.docs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.R;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;

public class DocListActivity extends FragmentActivity {

	public static final String TAG = "DocListActivity";
	private int selected;
	private DocsAdapter adapter;
	private ArrayList<Doc> docs = new ArrayList<Doc>();
	private boolean remove;
	private Account account;
	private ListView mList;

	// ---------------------------------------------------------------------------------------
	// Procedures onCreate

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doc_list_activity);
		mList = (ListView) findViewById(android.R.id.list);
		connexionServer();
		mList.setOnItemLongClickListener(docLongClick);
		mList.setOnItemClickListener(docClick);
		selected = -1;
		
		TextView name = (TextView) findViewById(R.id.doc_name);
		name.setText("Documents");
		
		adapter = new DocsAdapter(DocListActivity.this, R.layout.doc_row, docs);
		mList.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_doc_list, menu);
		MenuItem item = menu.findItem(R.id.doc_delete);
		item.setVisible(false);
		MenuItem itemAdd = menu.findItem(R.id.doc_add);
		if(getIntent().getBooleanExtra("add",false))
			itemAdd.setVisible(true);
		else
			itemAdd.setVisible(false);
		return true;
	}

	// ---------------------------------------------------------------------------------------
	// Procedures menu

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.doc_delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.progress_delete_title);
			builder.setMessage(R.string.menu_contextuel_mess);
			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.setPositiveButton(R.string.delete,
					new DialogInterface.OnClickListener() {
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
						for (Doc doc : docs)
							if (doc.isSelected())
								rm.add(doc);
						deleteDocs(rm);

					}
					remove = false;
				}
			});
			builder.create();
			builder.show();

			return true;
		case R.id.doc_add:
			DialogFragment dialog = DocUploadDialog.newInstance(getIntent().getExtras(), mListener);
			dialog.show(getSupportFragmentManager(), "dialog");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private DocUploadDialog.UploadListener mListener = new DocUploadDialog.UploadListener() {
		
		@Override
		public void onUpload(Doc newDoc) {
			if (newDoc != null) {
				docs.add(newDoc);
				adapter.notifyDataSetChanged();
			}
		}
	};
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.doc_delete);
		if(selected >= 0)
			item.setVisible(true);
		else
			item.setVisible(false);
		return true;
	}

	// -----------------------------------------------------------------------------------------------------
	// Procedure gestion des clics

	private OnItemClickListener docClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			if(selected == -1){
				String url = Constants.BASE_URL + "/"+ docs.get(position).getPath();
				Log.v(TAG, url);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
			}else {
				if(selected == position) {
					selected = -1;
					view.setBackground(null);
				}else {
					mList.getChildAt(selected).setBackground(null);
					selected = position;
					view.setBackgroundColor(Color.CYAN);
				}
			}
			invalidateOptionsMenu();
		}
	};

	private OnItemLongClickListener docLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view,
				int position, long id) {

			selected = position;
			if (checkItem(position))
				view.setBackgroundColor(Color.CYAN);
			else
				view.setBackground(null);
			invalidateOptionsMenu();
			return true;
		}
	};

	// --------------------------------------------------------------------------------------------
	// Fonctions utiles

	private boolean checkItem(int position) {
		Doc doc = adapter.getItem(position);
		doc.setSelected(!doc.isSelected());
		docs.get(position).setSelected(doc.isSelected());
		return doc.isSelected();
	}

//	private boolean afficheMenuDelete() {
//		for (int i = 0; i < docs.size(); i++) {
//			Doc doc = docs.get(i);
//			Log.v("selection", "" + doc.isSelected() + i);
//			if (doc.isSelected())
//				return true;
//		}
//		selection = false;
//		return false;
//	}

	private void deleteDocs(final List<Doc> rm) {

		new AsyncTask<Void, Void, Boolean>() {
			private ProgressDialog progress;
			@Override
			protected void onPreExecute() {
				progress = ProgressDialog.show(
						DocListActivity.this,
						getString(R.string.progress_delete_title),
						getString(R.string.progress_delete_infos));
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				boolean resultat = false;
				try {
					final String url = Constants.BASE_API_URL+ "/document/delete/";
					final String token = AccountHelper.blockingGetAuthToken(
							account, Constants.ACCOUNT_TOKEN_TYPE, true);

					account = AccountHelper.getAccount();
					for (int i = 0; i < rm.size(); i++) {
						Doc d = rm.get(i);
						Log.v("objet doc avant delete", "" + d.getName());
						String u = url + d.getId();
						Log.v("objet u", "" + u);
						HttpData get = new HttpData(u).header(Api.APP_AUTH_TOKEN, token).get();
						Log.v("retour du serveur", get.asString());
						JSONObject result = get.asJSONObject();
						Log.v("resultat de l'appel serveur","" + result.toString());
						Log.v("resultat succes","" + result.getBoolean("success"));
						resultat = result.getBoolean("success");
						if (result.getBoolean("success")) {
							docs.remove(d);
							Log.v("objet docs apres delete","" + docs.toString());
						}else{
							String err = result.getString("error");
							showError(err);
						}
					}
				} catch (HttpDataException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return resultat;
			}
			@Override
			protected void onPostExecute(Boolean result) {
				if(result){
					for (Doc doc : rm) docs.remove(doc);
					adapter.notifyDataSetChanged();
				}
				progress.dismiss();

				selected = -1;

				invalidateOptionsMenu();
			}
		}.execute();
	}

	private boolean connexionServer() {
		final ProgressDialog progress = ProgressDialog.show(
				DocListActivity.this, getString(R.string.progress_chargement),
				getString(R.string.progress_chargement_infos));
		account = AccountHelper.getAccount();
		final long id_promo = getIntent().getLongExtra("id_promo", 0);
		final long id_training = getIntent().getLongExtra("id_training", 0);
		final long id_establishment = getIntent().getLongExtra("id_establishment", 0);
		final long id_user = getIntent().getLongExtra("id_user", 0);
		Log.v("promo", "" + id_promo);
		Log.v("training", "" + id_training);
		Log.v("establishment", "" + id_establishment);
		Log.v("user", "" + id_user);
		final String url = Constants.BASE_API_URL + "/document";
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final String token = AccountHelper.blockingGetAuthToken(
							account, Constants.ACCOUNT_TOKEN_TYPE, true);
					HttpData http = new HttpData(url).header(Api.APP_AUTH_TOKEN, token);
					if(id_establishment > 0)
						http.data("id_establishment", String.valueOf(id_establishment));
					if(id_training > 0)
						http.data("id_training", String.valueOf(id_training));
					if(id_promo > 0)
						http.data("id_promo", String.valueOf(id_promo));
					if(id_user > 0)
						http.data("id_user", String.valueOf(id_user));
					JSONObject json = http.get().asJSONObject();
					Log.v("objet Json", "" + json.toString());
					if(json.has("error")){
						showError(json.getString("error"));
					}else{
						JSONArray result = json.getJSONArray("result");
						Log.v("objet RESULT", "" + result.toString());
						docs.clear();
						for (int i = 0; i < result.length(); i++) {
							JSONObject objet = result.getJSONObject(i);
							Doc d = new Doc().fromJSON(objet);
							docs.add(d);
						}
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								adapter.notifyDataSetChanged();
							}
						});
					}
				} catch (HttpDataException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					e.printStackTrace();
				} catch (IOException e) {
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

	protected void showError(final String err) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(DocListActivity.this, err, Toast.LENGTH_LONG).show();
			}
		});
	}

	// -------------------------------------------------------------------------------------
	// Class adapter view

	private class DocsAdapter extends ArrayAdapter<Doc> {

		private Context mContext;
		private int mRessource;
		private List<Doc> mItems;

		public DocsAdapter(Context context, int resource, List<Doc> objects) {
			super(context, resource, objects);
			mContext = context;
			mRessource = resource;
			mItems = objects;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null)
				view = LayoutInflater.from(mContext).inflate(mRessource,
						parent, false);

			Doc doc = mItems.get(position);
			TextView tv = (TextView) view.findViewById(R.id.titre);
			tv.setText(doc.getName());
			tv = (TextView) view.findViewById(R.id.desc);
			tv.setText(doc.getDescription());

			if (doc.isSelected())
				view.setBackgroundColor(Color.CYAN);
			else
				view.setBackground(null);

			return view;
		}

		@Override
		public Doc getItem(int position) {
			return mItems.get(position);
		}
	}

}

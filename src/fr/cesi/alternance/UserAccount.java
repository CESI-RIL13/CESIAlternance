package fr.cesi.alternance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.kolapsis.utils.DownloadImageTask;
import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.user.Link;
import fr.cesi.alternance.user.TypeEnum;
import fr.cesi.alternance.user.User;
import android.R.bool;
import android.os.Bundle;
import android.accounts.AuthenticatorException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class UserAccount extends FragmentActivity {

	private Button mBtAdd;
	private EditText mEditNom;
	private EditText mEditMail;
	private EditText mEditPhone;
	private ImageView mIVPhoto;
	private ListView mLVLinks;
	private User utilisateur;
	private boolean selection;
	private boolean remove;	
	private LinkAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);
		
		mBtAdd=(Button)findViewById(R.id.buttonAjoutLien);
		mEditNom=(EditText)findViewById(R.id.editTextNom);
		mEditMail=(EditText)findViewById(R.id.editTextMail);
		mEditPhone=(EditText)findViewById(R.id.editTextNumero);
		mIVPhoto=(ImageView)findViewById(R.id.imageViewPhoto);
		mLVLinks=(ListView)findViewById(R.id.listViewLinks);
		mBtAdd.setOnClickListener(mAddListener);
	    menuItems = new String[] { getResources().getString(R.string.menu_contextuel_open), getResources().getString(R.string.menu_contextuel_edit), getResources().getString(R.string.menu_contextuel_delete)};
		//mLVLinks.setOnItemLongClickListener(mSelectLink);
		mLVLinks.setOnItemClickListener(linkClick);
		selection=false;
		loadUser();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.menu_link_list, menu);
		
		menu.findItem(R.id.link_delete).setVisible(false);
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  if (v.getId()==R.id.listViewLinks) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    menu.setHeaderTitle(utilisateur.getLinks().get(info.position).getType().value());
	    for (int i = 0; i<menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	  }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.link_delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.progress_delete_title);
			builder.setMessage(R.string.menu_contextuel_mess);
			builder.setNegativeButton(R.string.menu_contextuel_cancel, null);
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
						List<Link> rm = new ArrayList<Link>();
						for(Link link : utilisateur.getLinks())
							if(link.isSelected()) rm.add(link);

						mAdapter.notifyDataSetChanged();
						afficheMenuDelete();
						invalidateOptionsMenu();
						deleteLinks(rm);
						for(Link link : rm)
							utilisateur.getLinks().remove(link);
					}
					remove = false;
				}
			});

			builder.create();
			builder.show();
		}
		return true;
	}
	
	protected void deleteLinks(final List<Link> rm) {
		// TODO Auto-generated method stub
		final ProgressDialog progress = ProgressDialog.show(UserAccount.this, getString(R.string.progress_delete_title), getString(R.string.progress_delete_infos));
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final String url = Constants.BASE_API_URL +"/user/deleteLink/";
					final String token = AccountHelper.blockingGetAuthToken(AccountHelper.getAccount(), Constants.ACCOUNT_TOKEN_TYPE, false);

					for(int i= 0;i<rm.size();i++) {
						Link l = rm.get(i);
						Log.v("objet link avant delete", ""+ l.getUrl());
						String u = url + l.getId();
						Log.v("objet u", ""+ u);
						HttpData get = new HttpData(u).header(Api.APP_AUTH_TOKEN, token).get();
						Log.v("retour du serveur", get.asString());
						JSONObject result = get.asJSONObject();
						Log.v("resultat de l'appel serveur", ""+ result.toString());
//						if(result.getBoolean("success")){
//							utilisateur.getLinks().remove(l);
//							Log.v("objet docs apres delete", ""+ utilisateur.getLinks().toString());
//						}
					}
				} catch (HttpDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
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

	private void loadUser() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					utilisateur = User.fromBundle(Api.getInstance().loadUser(AccountHelper.getData(Api.UserColumns.TOKEN)));
					showUser();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	protected void showUser() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mEditNom.setText(utilisateur.getName());
				mEditMail.setText(utilisateur.getMail());
				mEditPhone.setText(utilisateur.getPhone());
				String url = utilisateur.getPhoto();
				if(!TextUtils.isEmpty(url)) {
					mIVPhoto.setTag(Constants.BASE_URL + "/picture/" + url);
					new DownloadImageTask(mIVPhoto).execute();
				}
				mAdapter = new LinkAdapter(UserAccount.this, utilisateur.getLinks());
				mLVLinks.setAdapter(mAdapter);
				registerForContextMenu(mLVLinks);
			}
		});
	}
	
	private boolean checkItem(int position) {
		Link link = mAdapter.getItem(position);
		link.setSelected(!link.isSelected());

		return link.isSelected();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item =  menu.findItem(R.id.link_delete);
		if(afficheMenuDelete())
			item.setVisible(true);
		else item.setVisible(false);

		return true;
	}

	private boolean afficheMenuDelete() {
		if(utilisateur != null){
			ArrayList<Link> links = utilisateur.getLinks();
			for(int i = 0; i < links.size(); i++){
				Link link = links.get(i);
				Log.v("selection", ""+link.isSelected()+i);
				if(link.isSelected())
					return true;
			}
		}

		selection = false;
		return false;
	}

	private View.OnClickListener mAddListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			new AddLink().show(getSupportFragmentManager(), "TAG");
		}
	};

	private OnItemClickListener linkClick = new OnItemClickListener() {

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
	
	private OnItemLongClickListener mSelectLink = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			return true;
		}
	};
	private String[] menuItems;
	
	private class LinkAdapter extends ArrayAdapter<Link>{

		private LayoutInflater mInflater;
		private ArrayList<Link> mList;

		public LinkAdapter(Context context, ArrayList<Link> objects) {
			super(context, android.R.layout.simple_list_item_2, objects);
			mInflater = LayoutInflater.from(getContext());
			mList=objects;
			// TODO Auto-generated constructor stub
		}
		
		public ArrayList<Link> getObjects(){
			return mList;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) view = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
			Link link=getItem(position);
			TextView tv = (TextView) view.findViewById(android.R.id.text1);
			tv.setText(link.getType().value());
			tv = (TextView) view.findViewById(android.R.id.text2);
			tv.setText(link.getUrl());

			if(link.isSelected())
				view.setBackgroundColor(Color.CYAN);
			else view.setBackgroundColor(Color.WHITE);

			return view;
		}
		
	}
	
	private class AddLink extends DialogFragment{
		
		private Spinner spinner;
		private EditText url;
		private ArrayAdapter<String> adapter;
		
		public AddLink(){
			
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			View view = getActivity().getLayoutInflater().inflate(R.layout.add_link_layout, null, false);
			spinner=(Spinner) view.findViewById(R.id.spinnerTypeLink);
			url=(EditText) view.findViewById(R.id.editTextUrl);
			adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, TypeEnum.list());
			spinner.setAdapter(adapter);
			adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
			return new AlertDialog.Builder(getActivity())
								.setTitle("Ajout de lien")
								.setView(view)
								.setNegativeButton("Annuler", null)
								.setPositiveButton("Accepter", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										// TODO Auto-generated method stub
										addLink();
									}
								}).create();
		}

		protected void addLink() {
			// TODO Auto-generated method stub
			final String type=adapter.getItem(spinner.getSelectedItemPosition());
			final String url=this.url.getText().toString();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					utilisateur.addLink(type, url);
				}
			}).start();
		}
	}

}

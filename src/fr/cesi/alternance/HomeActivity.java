package fr.cesi.alternance;


import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.docs.DocListActivity;
import fr.cesi.alternance.promo.PromoListActivity;
import fr.cesi.alternance.training.Training;
import fr.cesi.alternance.training.TrainingActivity;

import java.util.ArrayList;
import java.util.Calendar;

import com.kolapsis.utils.StringUtils;

import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity implements AdapterView.OnItemClickListener {

	public static final String TAG = Constants.APP_NAME + ".HomeActivity";
	
	private Account mAccount;
	private HolderAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//AccountHelper.setContext(this);
		setContentView(R.layout.activity_home);
		
		AccountHelper.setContext(this);
		
		//deleteCalendar();

		Account[] accs = AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE);
		if (accs.length > 0) mAccount = accs[0];
		
		if (mAccount != null) {
			/*Bundle extras = new Bundle();
			extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(mAccount, Constants.PROVIDER_CALENDAR, extras);*/
			showApp();
		} else {
			showAccount();
		}
	}

    @SuppressWarnings("unused")
	private void deleteCalendar() {
		ContentResolver cr = getContentResolver();
		String[] projection = new String[]{ Calendars._ID, Calendars.ACCOUNT_NAME, Calendars.CALENDAR_DISPLAY_NAME, Calendars.CAL_SYNC1 };
        Cursor cur = cr.query(Calendars.CONTENT_URI, projection, null, null, null);
		while (cur.moveToNext()) {
			long calId = cur.getLong(0);
			String accName = cur.getString(1);
			String calName = cur.getString(2);
			String calSrcId = cur.getString(3);
			Log.i(TAG, "id " + calId + ", accName: " + accName + ", calName: " + calName + ", calSrcId: " + calSrcId);
			if ("RIL13".equals(calName) || "DW12".equals(calName)) {
				int rm = cr.delete(Events.CONTENT_URI, Events.CALENDAR_ID + " = " + calId, null);
				Log.d(TAG, "-> " + rm + " event deleted");
				rm = cr.delete(Calendars.CONTENT_URI, Calendars.ACCOUNT_NAME + " = '" + accName + "'", null);
				Log.d(TAG, "-> " + rm + " calendar deleted");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intentCompteUser = new Intent(this, UserAccount.class);
			startActivity(intentCompteUser);
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case Constants.RESULT_NEW_ACCOUNT:
			if (resultCode == Activity.RESULT_OK) {
				String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (!StringUtils.isEmpty(accountName)) {
					mAccount = new Account(accountName, Constants.ACCOUNT_TYPE);
					if (!AccountHelper.isAccount(mAccount)) AccountHelper.setAccount(mAccount);
				}
				if (AccountHelper.isEmpty()) showAccount();
				else showApp();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		Intent intent = mAdapter.getItemIntent(position);
		//Log.v(TAG, "onItemClick: " + intent);
		if (intent != null) startActivity(intent);
	}

	private void showAccount() {
		setContentView(R.layout.account_empty);
		Button addAccount = (Button) findViewById(R.id.account_add);
		addAccount.setVisibility(View.VISIBLE);
		addAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				chooseAccount();
			}
		});
		chooseAccount();
	}
	
	private void chooseAccount() {
		Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[] { Constants.ACCOUNT_TYPE }, true, null, Constants.ACCOUNT_TOKEN_TYPE, null, null);
		startActivityForResult(intent, Constants.RESULT_NEW_ACCOUNT, null);
	}
	
	private void showApp() {
		setContentView(R.layout.activity_home);

		Intent intent;
		ArrayList<Holder> buttons = new ArrayList<Holder>();
		final String role = AccountHelper.getRole();
		
		TextView name = (TextView) findViewById(R.id.name);
		name.setText(AccountHelper.getName());

		long startMillis = Calendar.getInstance().getTimeInMillis();
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath("time");
		ContentUris.appendId(builder, startMillis);
		
		intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
		Intent intentDocEtablissement = new Intent(this, DocListActivity.class);
		
		buttons.add(new Holder(getString(R.string.calendrier_title), getString(R.string.calendrier_action), intent));
		
		if ("IF".equals(role)) {
			
			Intent intentFormations = new Intent(this, TrainingActivity.class);
			intentDocEtablissement.putExtra("add", true);
			
			buttons.add(new Holder(getString(R.string.training_title), getString(R.string.training_action), intentFormations));
		
		} else if ("Intervenant".equals(role)) {			

			Intent intentFormations = new Intent(this, TrainingActivity.class);
			
			buttons.add(new Holder(getString(R.string.training_title), getString(R.string.training_action), intentFormations));		
		
		} else if ("Stagiaire".equals(role)) {
			
			Intent intentPromos = new Intent(this, PromoListActivity.class);
			Training training = new Training(0);
			training.setName("Mes Formations");
			intentPromos.putExtra("training", training);
			
			buttons.add(new Holder(getString(R.string.promo_title), getString(R.string.promo_action), intentPromos));

		}
		
		intentDocEtablissement.putExtra("id_establishment", 1L);

		buttons.add(new Holder(getString(R.string.doc_establishment_title), getString(R.string.doc_establishment_action), intentDocEtablissement));
		
		Intent intentDoc = new Intent(this, DocListActivity.class);
		intentDoc.putExtra("add", true);
		Log.v(TAG, "Id : " + AccountHelper.getUserId());
		intentDoc.putExtra("id_user", AccountHelper.getUserId());
		buttons.add(new Holder(getString(R.string.doc_title), getString(R.string.doc_action), intentDoc));
		
		mAdapter = new HolderAdapter(this, buttons);
		ListView list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		list.setAdapter(mAdapter);
	}

	private class Holder {
		private final String mTitle;
		private final String mSubTitle;
		private final Intent mIntent;
		public Holder(String t, String s, Intent i) {
			mTitle = t;
			mSubTitle = s;
			mIntent = i;
		}
		public String getTitle() {
			return mTitle;
		}
		public String getSubTitle() {
			return mSubTitle;
		}
		public Intent getIntent() {
			return mIntent;
		}
	}

	private class HolderAdapter extends ArrayAdapter<Holder> {
		private LayoutInflater mInflater;
		private ArrayList<Holder> mItems;
		public HolderAdapter(Context context, ArrayList<Holder> items) {
			super(context, android.R.layout.simple_list_item_2, items);
			mInflater = LayoutInflater.from(getContext());
			mItems = items;
		}
		@Override
		public Holder getItem(int position) {
			return mItems.get(position);
		}
		public Intent getItemIntent(int position) {
			return mItems.get(position).getIntent();
		}
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) view = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
			TextView tv = (TextView) view.findViewById(android.R.id.text1);
			tv.setText(mItems.get(position).getTitle());
			tv = (TextView) view.findViewById(android.R.id.text2);
			tv.setText(mItems.get(position).getSubTitle());
			return view;
		}
	}
}
package fr.cesi.alternance.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kolapsis.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.helpers.CalendarHelper;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class CalendarService extends Service {

	public static final String TAG 							= Constants.APP_NAME + ".CalendarSyncService";

	private static SyncAdapterImpl sSyncAdapter = null;

	private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

		private Context mContext;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
			try {
				CalendarService.performSync(mContext, account, extras, authority, provider, syncResult);
			} catch (OperationCanceledException e) {
				Log.e("AuthService", "OperationCanceledException: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return getSyncAdapter().getSyncAdapterBinder();
	}

	private SyncAdapterImpl getSyncAdapter() {
		if (sSyncAdapter == null) sSyncAdapter = new SyncAdapterImpl(this);
		return sSyncAdapter;
	}

	private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) throws OperationCanceledException {
		Log.i("AuthService", "Calendar Service performSync " + account.name);
        AccountHelper.setContext(context);
		CalendarHelper.setContext(context);
		try {
			final String authtoken = AccountHelper.blockingGetAuthToken(account, Constants.ACCOUNT_TOKEN_TYPE, false);
			if (!StringUtils.isEmpty(authtoken)) {
				List<CalendarHelper.Planning> pRemotes = Api.getInstance().load(CalendarHelper.Planning.class, account);
                List<CalendarHelper.Planning> pLocals  = CalendarHelper.Planning.select(account);
				for (CalendarHelper.Planning pLocal : pLocals) {
					int index = pRemotes.indexOf(pLocal);
					if (index == -1) CalendarHelper.Planning.delete(account, pLocal.getId());
				}
				for (CalendarHelper.Planning pRemote : pRemotes) {
                    CalendarHelper.Planning pLocal = CalendarHelper.Planning.select(account, pRemote.getSourceId());
                    if (pLocal == null) {
                        //Log.v(TAG, "insert " + pRemote.getSourceId());
                        long id = CalendarHelper.Planning.insert(account, pRemote);
                        if (id > 0) pRemote.setId(id);
                    } else {
                        pRemote.setId(pLocal.getId());
                        if (CalendarHelper.Planning.hasChange(pLocal, pRemote)) {
                            //Log.v(TAG, "update " + pRemote.getId() + ", " + pRemote.getSourceId());
                            CalendarHelper.Planning.update(account, pRemote);
                        }
                    }
                    if (pRemote.getId() > 0) {
                        Log.v(TAG, pRemote.getName() + ", " + pRemote.getSourceId());
                        List<CalendarHelper.Event> eRemotes = pRemote.getEvents();
                        List<CalendarHelper.Event> eLocals  = CalendarHelper.Event.select(account, pRemote.getId());
                        for (CalendarHelper.Event eLocal : eLocals) {
                            int index = eRemotes.indexOf(eLocal);
                            if (index == -1) CalendarHelper.Event.delete(account, eLocal.getId());
                        }
                        for (CalendarHelper.Event eRemote : eRemotes) {
                            CalendarHelper.Event eLocal = CalendarHelper.Event.select(account, pRemote.getId(), eRemote.getSourceId());
                            if (eLocal == null) {
                                //Log.v(TAG, "-> insert " + eRemote.getSourceId());
                                long id = CalendarHelper.Event.insert(account, pRemote.getId(), eRemote);
                                if (id > 0) eRemote.setId(id);
                            } else {
                                eRemote.setId(eLocal.getId());
                                if (CalendarHelper.Event.hasChange(eLocal, eRemote)) {
                                    //Log.v(TAG, "-> update " + eRemote.getId() + ", " + eRemote.getSourceId());
                                    CalendarHelper.Event.update(account, eRemote);
                                }
                            }
                        }
                    }
				}
			}
		} catch (AuthenticatorException e) {
			Log.e("CalendarService", "AuthenticatorException: " + e.getMessage(), e);
		} catch (IOException e) {
			Log.e("CalendarService", "IOException: " + e.getMessage(), e);
		}
		
		/*
		try {
			//String url = "http://www.google.com/calendar/feeds/" + Constants.DEBUG_CALENDAR_REFERENCE + "@group.calendar.google.com/public/full?alt=json";
			String url = Constants.BASE_API_URL + "/calendar";
			HttpData request = new HttpData(url).get();
			JSONObject json = request.asJSONObject();
			//Log.v(TAG, "json: " + json);
			JSONObject feed = json.getJSONObject("feed");
			String title = feed.getJSONObject("title").getString("$t");
			long calId = CalendarHelper.select(account, title);
			//Log.v(TAG, "title: " + title);
			if (calId == 0) calId = CalendarHelper.insert(account, title, Constants.DEBUG_CALENDAR_REFERENCE);
			JSONArray entries = feed.getJSONArray("entry");
			//Log.v(TAG, entries.length() + " entries");
			for (int i=0; i<entries.length(); i++) {
				JSONObject entry = entries.getJSONObject(i);
				String sourceId = entry.getJSONObject("id").getString("$t");
				int index = sourceId.lastIndexOf("/");
				sourceId = sourceId.substring(index+1);
				title = entry.getJSONObject("title").getString("$t");
				String content = entry.getJSONObject("content").getString("$t");
				JSONObject dates = entry.getJSONArray("gd$when").getJSONObject(0);
				String begin = dates.getString("startTime");
				String end = dates.getString("endTime");
				String where = entry.getJSONArray("gd$where").getJSONObject(0).getString("valueString");
				long evtId = CalendarHelper.Event.select(account, calId, sourceId);
				if (evtId == 0) CalendarHelper.Event.insert(account, calId, sourceId, title, content, begin, end, where);
			}
		} catch (HttpDataException e) {
			Log.e("AuthService", "HttpDataException: " + e.getMessage(), e);
		} catch (JSONException e) {
			Log.e("AuthService", "JSONException: " + e.getMessage(), e);
		}
		*/
	}

}

package fr.cesi.alternance.services;

import java.io.IOException;
import java.util.List;

import com.kolapsis.utils.StringUtils;
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
	}

}

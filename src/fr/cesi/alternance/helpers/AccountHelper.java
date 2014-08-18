package fr.cesi.alternance.helpers;

import java.io.IOException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AccountHelper {
	
	public static final String TAG = Constants.APP_NAME + ".AccountHelper";
	
	public static final int SYNC_FINISH = 0;
	public static final int SYNC_PROGRESS = 1;
	
	private static Context sContext;
	private static AccountManager sManager;
	private static Account sAccount;
	
	private AccountHelper() {}
	
	public static final void setContext(Context context) {
		if (context != sContext) {
			//Log.i(TAG, "-> setContext: " + context.getPackageName());
			sContext = context;
			CalendarHelper.setContext(sContext);
			Api.setContext(sContext);
		}
		sManager = AccountManager.get(sContext);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
		if (sAccount == null && prefs.contains("selected_account")) {
			String name = prefs.getString("selected_account", null);
			if (name != null) sAccount = getAccountByTypeAndName(Constants.ACCOUNT_TYPE, name);
		}
	}
	
	public static final String blockingGetAuthToken(Account account, String type, boolean manual) throws AuthenticatorException, IOException {
		if (Constants.DEBUG) Log.d(TAG, "blockingGetAuthToken('"+account.name+"', '"+type+"')");
		String authtoken = null;
		try {
			authtoken = sManager.blockingGetAuthToken(account, type, true);
			if (Constants.ACCOUNT_TOKEN_TYPE.equals(type)) {
				boolean valide = Api.getInstance().isValideToken(account, authtoken);
				if (Constants.DEBUG) Log.d(TAG, "-> isValideToken: " + valide);
				if (!valide) {
					if (authtoken != null) sManager.invalidateAuthToken(type, authtoken);
					sManager.setAuthToken(account, type, null);
					authtoken = sManager.blockingGetAuthToken(account, type, true);
				}
			}
		} catch (OperationCanceledException e) {
			Log.e(TAG, "OperationCanceledException" + e.getMessage());
		} finally {
			sManager.setUserData(account, Api.UserColumns.TOKEN, authtoken);
		}
		//if (Constants.DEBUG) Log.d(TAG, "-> token: " + authtoken);
		return authtoken;
	}
	
	public static final Account[] getAccountsByType(String type) {
		return sManager.getAccountsByType(type);
	}
	public static final Account getAccountByType(String type) {
		if (Constants.ACCOUNT_TYPE.equals(type)) return sAccount;
		return null;
	}
	public static final Account getAccountByTypeAndName(String type, String name) {
		Account[] accounts = sManager.getAccountsByType(type);
		for (Account account : accounts) {
			if (name.equals(account.name)) return account;
		}
		return null;
	}
	
	public static final boolean isEmpty() {
		return !hasAccount();
	}
	
	public static final boolean isAccount(Account account) {
		if (Constants.ACCOUNT_TYPE.equals(account.type)) {
			if (!hasAccount()) return false;
			return getAccount().name.equals(account.name);
		}
		return false;
	}
	public static final void setAccount(Account account) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
		boolean change = false;
		if (Constants.ACCOUNT_TYPE.equals(account.type) && !account.equals(sAccount)) {
			sAccount = account;
			prefs.edit().putString("selected_account", sAccount.name).commit();
			change = true;
		}
		Log.v(TAG, "setCurrentAccount: " + change + " > " + account);
		//if (change) dispatchEvent(new Event(ACCOUNT_CHANGE, account));
	}
	
	public static final String getData(Account account, String name) {
		if (account == null) return null;
		String value = sManager.getUserData(account, name);
		//Log.v(TAG, "getBoxData: " + name + " = " + value);
		return value;
	}
	
	public static final boolean hasAccount() {
		if (sManager == null) return false;
		return sManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0;
	}
	public static final Account getAccount() {
		if (sAccount == null && hasAccount()) {
			Account[] accounts = sManager.getAccountsByType(Constants.ACCOUNT_TYPE);
			sAccount = accounts[0];
		}
		return sAccount;
	}
	public static final String getData(String name) {
		return getData(getAccount(), name);
	}
	
	public static final String getName() {
		return getData(Api.UserColumns.NAME);
	}
	public static final String getRole() {
		return getData(Api.UserColumns.ROLE);
	}

	public static final int getUserId() {
		return getUserId(getAccount());
	}
	public static final int getUserId(Account account) {
		return Integer.parseInt(getData(account, Api.UserColumns.ID));
	}

	public static final String getPassword() {
		return getPassword(getAccount());
	}
	public static final String getPassword(Account account) {
		return sManager.getPassword(account);
	}

}
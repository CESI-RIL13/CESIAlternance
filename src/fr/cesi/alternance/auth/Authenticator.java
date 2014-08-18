package fr.cesi.alternance.auth;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.api.Api;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {

	public static final String TAG 							= Constants.APP_NAME + ".Authenticator";
	
	public static final String PARAM_CONFIRM_CREDENTIALS 	= "confirmCredentials";
	public static final String PARAM_ACCOUNT_NAME 			= "identifier";
	public static final String PARAM_USERNAME 				= "username";
	public static final String PARAM_AUTHTOKEN_TYPE 		= "authtokenType";

	private final Context mContext;

	public Authenticator(Context context) {
		super(context);
		//Log.i(TAG, "onCreate();");
		mContext = context;
	}
	
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
		//Log.v(TAG, "addAccount("+accountType+")");
		Intent intent = getAuthIntent(accountType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
		Log.v(TAG, "confirmCredentials()");
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		Log.v(TAG, "editProperties()");
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws NetworkErrorException {
		Log.d(TAG, "-> getAuthToken("+authTokenType+")");
		// If the caller requested an authToken type we don't support, then
		// return an error
		if (!isValideToken(authTokenType)) {
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
			return result;
		}

		// Extract the username and password from the Account Manager, and ask
		// the server for an appropriate AuthToken.
		final AccountManager am = AccountManager.get(mContext);
		final String challenge = am.getPassword(account);
		Intent intent = null;
		if (challenge != null) {
			String authToken = null;
			try { authToken = Api.getInstance().authentificate(account); }
			catch (AuthenticatorException e) { Log.e(TAG, "AuthenticatorException: " + e.getMessage()); }
			Log.i(TAG, "--> authToken: " + authToken);
			
			if (authToken != null) {
				final Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, getAccountType(authTokenType));
				result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
				return result;
			} else {
				intent = getAuthIntent(authTokenType);
				intent.putExtra(PARAM_ACCOUNT_NAME, account.name);
				intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
				intent.putExtra(PARAM_CONFIRM_CREDENTIALS, true);
				//intent.putExtra(PARAM_USERNAME, am.getUserData(account, Box.UserColumns.MAC_ADDRESS));
			}
		} else {
			intent = getAuthIntent(authTokenType);
			intent.putExtra(PARAM_ACCOUNT_NAME, account.name);
			intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		}
		
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// null means we don't support multiple authToken types
		Log.v(TAG, "getAuthTokenLabel()");
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
		// This call is used to query whether the Authenticator supports
		// specific features. We don't expect to get called, so we always
		// return false (no) for any queries.
		Log.v(TAG, "hasFeatures()");
		final Bundle result = new Bundle();
		result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
		return result;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) {
		Log.v(TAG, "updateCredentials()");
		return null;
	}
	private boolean isValideToken(String authTokenType) {
		return authTokenType.equals(Constants.ACCOUNT_TOKEN_TYPE);
	}
	private String getAccountType(String authTokenType) {
		String type = null;
		if (authTokenType.equals(Constants.ACCOUNT_TOKEN_TYPE)) type = Constants.ACCOUNT_TYPE;
		return type;
	}
	private Intent getAuthIntent(String type) {
		if (type.equals(Constants.ACCOUNT_TYPE) || type.equals(Constants.ACCOUNT_TOKEN_TYPE)) return new Intent(mContext, AccountActivity.class);
		return null;
	}
}

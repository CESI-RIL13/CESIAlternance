package fr.cesi.alternance.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kolapsis.utils.CloseUtils;
import com.kolapsis.utils.HttpData;
import com.kolapsis.utils.HttpData.HttpDataException;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.helpers.AccountHelper;
import fr.cesi.alternance.helpers.CalendarHelper;
import fr.cesi.alternance.helpers.Entity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class Api {
	
	public static final String TAG = Constants.APP_NAME + ".Api";
	
	public static String APP_AUTH_TOKEN 					= "X-CESI-App-Auth";
	private static final int BACKOFF_MILLI_SECONDS 			= 6 * 1000;
	private static boolean AUTH_IN_PROGRESS 				= false;
	
	private static Api sInstance;
	public static final void setContext(Context context) {
		//Log.i(TAG, "--> setContext: " + context.getPackageName());
		CalendarHelper.setContext(context);
	}

	public static final Api getInstance() {
		if (sInstance == null) sInstance = new Api();
		return sInstance;
	}

	public static interface UserColumns {
		public static final String TOKEN				= "token";
		public static final String ID					= "id";
		public static final String ROLE					= "role";
		public static final String NAME					= "name";
		public static final String EMAIL				= "email";
		public static final String PASSWORD				= "pwd";
		public static final String ERROR				= "error";
		public static final String PHONE				= "phone";
		public static final String PICTURE_PATH			= "picture_path";
		public static final String LINKS				= "links";
	}

	private final Bundle mUser;
	private HttpContext mHttpContext;
	private String mApiUrl;

	private Api() {
		mUser = new Bundle();
		initialize();
		setApiUrl(null);
	}
	
	private void initialize() {
		mUser.clear();
		mHttpContext = getHttpContext();
	}
	
	private void setApiUrl(String url) {
		mApiUrl = url == null ? Constants.BASE_API_URL : url;
		//Log.d(TAG, "--> setApiUrl: " + mApiUrl);
	}
	public String getApiUrl() {
		return mApiUrl;
	}
	
	private HttpContext getHttpContext() {
		HttpContext context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
		return context;
	}

	private AbstractHttpClient getHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.NETSCAPE);
		params.setParameter(ClientPNames.DEFAULT_HOST, "mobile.free.fr");
		params.setParameter(CoreProtocolPNames.ORIGIN_SERVER, "https://mobile.free.fr");
		params.setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4");
		return new DefaultHttpClient(params);
	}
	
	private HttpData getHttpData(Account account, String url) {
		HttpData request = new HttpData(getHttpClient(), url);
		if (account != null) {
			String token = AccountHelper.getData(UserColumns.TOKEN);
			if (!TextUtils.isEmpty(token)) request.header(APP_AUTH_TOKEN, token);
		}
		//Log.d(TAG+".Config", "--> getHttpData('" + FBX_APP_AUTH + "', '" + token + "')");
		return request;
	}
	
	/**
	 * Validation d'un token d'authentification
	 * @param account
	 * @param token
	 * @return success
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	
	public boolean isValideToken(Account account, String token) throws AuthenticatorException, IOException {
		if (token == null) return false;
		while (AUTH_IN_PROGRESS) {
			try {
				//if (Constants.DEBUG) Log.i(TAG, "--> starting wait");
				Thread.sleep(BACKOFF_MILLI_SECONDS);
				//if (Constants.DEBUG) Log.i(TAG, "--> continue");
				if (mUser.containsKey(UserColumns.TOKEN)) return mUser.getString(UserColumns.TOKEN).equals(token);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				AUTH_IN_PROGRESS = false;
			}
		}
		boolean success = false;
		HttpData request = null;
		try {
			String url = mApiUrl + "/verify";
			//Log.d(TAG+".Auth", "--> isValideToken('" + url + "', '" + token + "')");
			request = getHttpData(null, url).header(APP_AUTH_TOKEN, token).get(mHttpContext);
			JSONObject json = request.asJSONObject();
			success = request.isHttpOK() && json.getBoolean("success");
			//Log.d(TAG+".Auth", "--> token: '" + token + "', valide: '" + success + "'");
		} catch (HttpDataException e) {
			Log.e(TAG+".Auth", "HttpDataException: " + e.getMessage());
			throw new IOException(e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG+".Auth", "JSONException: " + e.getMessage());
			throw new AuthenticatorException(e.getMessage());
		} finally {
			CloseUtils.closeQuietly(request);
		}
		return success;
	}
	
	/**
	 * Authentification via Bundle
	 * @param data
	 * @return token
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	public String authentificate(Bundle data) throws AuthenticatorException, IOException {
		if (data == null) return null;
		String email = data.getString(UserColumns.EMAIL, null);
		String password = data.getString(UserColumns.PASSWORD, null);
		if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) return null;
		return authentificate(email, password);
	}
	
	/**
	 * Authentification via Account
	 * @param account
	 * @return token
	 * @throws AuthenticatorException
	 */
	public String authentificate(Account account) throws AuthenticatorException {
		if (account == null) return null;
		try {
			String email = AccountHelper.getData(UserColumns.EMAIL);
			String password = AccountHelper.getPassword();
			if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) return null;
			return authentificate(email, password);
		} catch (AuthenticatorException e) {
			Log.e(TAG, "AuthenticatorException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Authentification via Email / Password
	 * @param email
	 * @param password
	 * @return token
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	private String authentificate(String email, String password) throws AuthenticatorException, IOException {
		String url = mApiUrl + "/user/login";
		Log.d(TAG+".Auth", "--> authentificate('" + url + "', '" + email + "', '" + password + "')");
		Log.v(TAG+".Auth", "---> auth in progress: " + AUTH_IN_PROGRESS);
		while (AUTH_IN_PROGRESS) {
			try {
				if (Constants.DEBUG) Log.i(TAG, "--> starting wait");
				Thread.sleep(BACKOFF_MILLI_SECONDS);
				if (mUser.containsKey(UserColumns.TOKEN)) return mUser.getString(UserColumns.TOKEN);
				else if (Constants.DEBUG) Log.i(TAG, "--> continue");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				AUTH_IN_PROGRESS = false;
			}
		}
		mUser.remove(UserColumns.TOKEN);
		String token = null;
		HttpData request = null;
		try {
			request = getHttpData(null, url)
					.data("email", email).data("password", password)
					.post(mHttpContext);
			Log.v(TAG+".Auth", "result:" + request.asString());
			JSONObject json = request.asJSONObject();
			boolean success = request.isHttpOK() && json.getBoolean("success");
			if (!success) throw new AuthenticatorException(json.getString(UserColumns.ERROR));
			else {
				JSONObject result = json.getJSONObject("result");
				token = result.getString(UserColumns.TOKEN);
				mUser.putString(UserColumns.TOKEN, token);
				Log.d(TAG+".Auth", "--> token: '" + token + "'");
			}
		} catch (HttpDataException e) {
			Log.e(TAG+".Auth", "HttpDataException: " + e.getMessage());
			throw new IOException(e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG+".Auth", "JSONException: " + e.getMessage());
			throw new AuthenticatorException(e.getMessage());
		} finally {
			CloseUtils.closeQuietly(request);
		}
		return token;
	}
	
	/**
	 * Chargement des donn�es d'un compte authentifi�
	 * @param token
	 * @return Les donn�es de l'utilisateur
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	public Bundle loadUser(String token) throws AuthenticatorException, IOException {
		Bundle data = new Bundle();
		HttpData request = null;
		try {
			String url = mApiUrl + "/user/load";
			Log.d(TAG+".Auth", "---> loadUser('" + url + "', '" + token + "')");
			request = getHttpData(null, url).header(APP_AUTH_TOKEN, token).get(mHttpContext);
			Log.v(TAG, request.asString());
			JSONObject json = request.asJSONObject();
			boolean success = request.isHttpOK() && json.getBoolean("success");
			Log.d(TAG+".Auth", "---> success: " + success);
			if (success) {
				JSONObject result = json.getJSONObject("result");
				data.putString(UserColumns.ID, String.valueOf(result.getInt(UserColumns.ID)));
				data.putString(UserColumns.NAME, result.getString(UserColumns.NAME));
				data.putString(UserColumns.ROLE, result.getString(UserColumns.ROLE));
				data.putString(UserColumns.PHONE, result.getString(UserColumns.PHONE));
				data.putString(UserColumns.PICTURE_PATH, result.getString(UserColumns.PICTURE_PATH));
				data.putString(UserColumns.EMAIL, result.getString(UserColumns.EMAIL));
				data.putString(UserColumns.LINKS, json.getJSONArray(UserColumns.LINKS).toString());
				//Log.v(TAG, data.getString(UserColumns.LINKS));
			}
		} catch (HttpDataException e) {
			Log.e(TAG+".Auth", "HttpDataException: " + e.getMessage());
			throw new IOException(e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG+".Auth", "JSONException: " + e.getMessage());
			throw new AuthenticatorException(e.getMessage());
		} finally {
			CloseUtils.closeQuietly(request);
		}
		return data;
	}
	
	/**
	 * TODO : DATA LOADER, SAVER, DELETER
	 */
	
	/**
	 * Chargement des Entit�s
	 * @param clzz Class de l'entit� a utiliser.
	 * @param account Compte a synchroniser
	 * @return Une <b>liste</b> d'entit� <b>&lt;Entity&gt</b>
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	public <T extends Entity> List<T> load(Class<T> clzz, Account account) throws AuthenticatorException, IOException {
		return load(clzz, account, 0);
	}
	
	/**
	 * Chargement des Entit�s
	 * @param clzz Class de l'entit� a utiliser.
	 * @param account Compte a synchroniser
	 * @param lastSync Timestamp (long) de la derni�re synchronisation
	 * @return Une <b>liste</b> d'entit� <b>&lt;Entity&gt</b>
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	public <T extends Entity> List<T> load(Class<T> clzz, Account account, long lastSync) throws AuthenticatorException, IOException {
		if (Constants.DEBUG) lastSync = 0;
		List<T> list = new ArrayList<T>();
		HttpData request = null;
		try {
			T inst = clzz.newInstance();
			String url = mApiUrl + "/" + inst.getApiPath();
			Log.d(TAG+".Load", "---> load('" + url + "')");
			request = getHttpData(account, url);
			if (lastSync > 0) request.data("last_update", String.valueOf((int) (lastSync / 1000)));
			request.get(mHttpContext);
			//Log.d(TAG+".Load", request.asString());
			JSONObject json = request.asJSONObject();
			boolean success = request.isHttpOK() && json.getBoolean("success");
			if (!success) throw new AuthenticatorException(json.getString(UserColumns.ERROR));
			else {
				JSONArray ints = json.getJSONArray("result");
				for (int i=0; i<ints.length(); i++) {
					JSONObject inter = ints.getJSONObject(i);
					inst = clzz.newInstance();
					inst.fromJSON(inter);
					list.add(inst);
				}
			}
		} catch (HttpDataException e) {
			Log.e(TAG+".Load", "HttpDataException: " + e.getMessage());
			throw new IOException(e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG+".Load", "JSONException: " + e.getMessage());
			throw new AuthenticatorException(e.getMessage());
		} catch (InstantiationException e) {
			Log.e(TAG+".Load", "InstantiationException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(TAG+".Load", "IllegalAccessException: " + e.getMessage());
		} finally {
			CloseUtils.closeQuietly(request);
		}
		return list;
	}
	
	/** 
	 * Ajout d'une entit�
	 * @param clzz Class de l'entit� a utiliser.
	 * @param account Compte a synchroniser
	 * @param entity Entit� a sauvegarder
	 * @return Un <b>entier</b> positif correspondant au nouvel identifiant distant, ou 0 si une erreur est survenue
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	public <T extends Entity> int save(Class<T> clzz, Account account, Entity entity) throws AuthenticatorException, IOException {
		int sourceId = 0;
		HttpData request = null;
		try {
			T inst = clzz.newInstance();
			String url = mApiUrl + "/" + inst.getApiPath();
			if (entity.getId() > 0) url += "/" + entity.getId();
			Log.d(TAG+".Save", "---> save('" + url + "')");
			//Log.d(TAG+".Auth", "---> json: " + InterventionsHelper.Intervention.asJSON(intervention) + "");
			request = getHttpData(account, url).data(entity.asJSON()).post(mHttpContext);
			JSONObject json = request.asJSONObject();
			boolean success = request.isHttpOK() && json.getBoolean("success");
			if (!success) throw new AuthenticatorException(json.getString(UserColumns.ERROR));
			sourceId = json.getInt("id");
		} catch (HttpDataException e) {
			Log.e(TAG+".Save", "HttpDataException: " + e.getMessage());
			throw new IOException(e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG+".Save", "JSONException: " + e.getMessage());
			throw new AuthenticatorException(e.getMessage());
		} catch (InstantiationException e) {
			Log.e(TAG+".Save", "InstantiationException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(TAG+".Save", "IllegalAccessException: " + e.getMessage());
		} finally {
			CloseUtils.closeQuietly(request);
		}
		return sourceId;
	}
	
	/**
	 * Suppression d'une entit�
	 * @param clzz Class de l'entit� a utiliser.
	 * @param account Compte a synchroniser
	 * @param remoteId Identifiant distant de l'entit� � supprimer
	 * @return Un <b>bool�en</b>, <b>true</b> si la suppression est effectu�e avec succ�s, sinon <b>false</b>
	 * @throws AuthenticatorException
	 * @throws IOException
	 */
	public <T extends Entity> boolean delete(Class<T> clzz, Account account, Entity entity) throws AuthenticatorException, IOException {
		boolean success = false;
		HttpData request = null;
		try {
			T inst = clzz.newInstance();
			String url = mApiUrl + "/" + inst.getApiPath() + "/" + entity.getId();
			Log.d(TAG+".Delete", "---> delete('" + url + "')");
			request = getHttpData(account, url).delete(mHttpContext);
			JSONObject json = request.asJSONObject();
			success = request.isHttpOK() && json.getBoolean("success");
			if (!success) throw new AuthenticatorException(json.getString(UserColumns.ERROR));
		} catch (HttpDataException e) {
			Log.e(TAG+".Delete", "HttpDataException: " + e.getMessage());
			throw new IOException(e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG+".Delete", "JSONException: " + e.getMessage());
			throw new AuthenticatorException(e.getMessage());
		} catch (InstantiationException e) {
			Log.e(TAG+".Delete", "InstantiationException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(TAG+".Delete", "IllegalAccessException: " + e.getMessage());
		} finally {
			CloseUtils.closeQuietly(request);
		}
		return success;
	}
	
}

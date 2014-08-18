package fr.cesi.alternance.services;

import fr.cesi.alternance.Constants;
import fr.cesi.alternance.auth.Authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthService extends Service {

	public static final String TAG 							= Constants.APP_NAME + ".AuthService";

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        //Log.v(TAG, "Authentication Service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.v(TAG, "getBinder()...  returning the AccountAuthenticator binder for intent " + intent);
        return mAuthenticator.getIBinder();
    }
}

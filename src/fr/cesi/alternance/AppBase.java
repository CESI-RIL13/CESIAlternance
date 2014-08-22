package fr.cesi.alternance;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class AppBase extends Application {

	private boolean mNetworkState;
	
	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, filter);
	}
	@Override
	public void onTerminate() {
		unregisterReceiver(mReceiver);
		super.onTerminate();
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean currentState = isNetworkAvailable(getApplicationContext());
			if(currentState != mNetworkState){
				mNetworkState = currentState;
				if(!mNetworkState){
					Toast.makeText(getApplicationContext(), "Perte de réseau", Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	public boolean isNetworkAvailable(Context context) {
		boolean isMobile = false, isWifi = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] infoAvailableNetworks = cm.getAllNetworkInfo();

		if (infoAvailableNetworks != null) {
			for (NetworkInfo network : infoAvailableNetworks) {

				if (network.getType() == ConnectivityManager.TYPE_WIFI) {
					if (network.isConnected() && network.isAvailable())
						isWifi = true;
				}
				if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
					if (network.isConnected() && network.isAvailable())
						isMobile = true;
				}
			}
		}

		return isMobile || isWifi;
	}
}

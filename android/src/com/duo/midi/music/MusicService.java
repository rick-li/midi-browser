package com.duo.midi.music;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.duo.midi.MainActivity;
import com.duo.midi.MusicFragment;
import com.duo.midi.TimeCounter;
import com.duo.midi.alarm.WakefulIntentService;

public class MusicService extends WakefulIntentService {
	private static final String TAG = "MusicService";

	// private boolean isFirstRound = true;
	public MusicService() {
		super("MusicService");
	}

	Timer networkWaitTimer = new Timer();
	long maxNetworkWaitTime = 60 * 1000;

	@Override
	protected void doWakefulWork(final Context context, Intent intent) {
		Log.i(TAG, "I'm awake! I'm awake! (yawn)");
		if (context == null) {
			return;
		}
		final TimeCounter counter = new TimeCounter();
		networkWaitTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				counter.increaseOneSec();
				if (checkNetworkStatus()) {
					stopNetworkWaitTimer();
					restartMusic(context);
				}else{
					Log.d(TAG, "Network connection not retrieved yet, wait...");
					return;
				}
				if (counter.getStartMillSec() == maxNetworkWaitTime) {
					stopNetworkWaitTimer();
					Log.e(TAG, "Unable to get network connected after sleep.");
				}
			}

		}, 0, 1000);
		// wait until the network is active, if not within the given time,
		// cancel the alarm and cancel.

	}
	private void stopNetworkWaitTimer(){
		if(networkWaitTimer!=null){
			networkWaitTimer.cancel();
			networkWaitTimer = null;
			
		}
	}
	private void restartMusic(Context context){
		Log.i(TAG, "Restarting Music.");
		MainActivity activity = (MainActivity) context;
		MusicFragment mf = activity.getMusicFragment();
		if (mf.isNeedRepeat()) {
			mf.stopWaitTimer();

			mf.getWebView().reload();
		}		
	}
	public boolean checkNetworkStatus() {

		final ConnectivityManager connMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isAvailable()) {
			if (wifi.getState() == NetworkInfo.State.CONNECTED) {
				Log.i(TAG, "Wifi is connected.");
				return true;
			} else {
				Log.i(TAG, "Wifi available but NOT connected.");
				return false;
			}
		} else if (mobile.isAvailable()) {
			if (mobile.getState() == NetworkInfo.State.CONNECTED) {
				Log.i(TAG, "GPRS is connected.");

				return true;
			} else {
				Log.i(TAG, "GPRS available but NOT connected.");
				return false;
			}

		} else {
			Log.i(TAG, "NO network connection.");
			return false;
		}

	}
}
package com.duo.midi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service {
	public MusicService() {
	}

	private static final String TAG = "MusicService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private MediaPlayer mp;

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		stopMusic();
	}

	Timer timer = new Timer();
	WifiLock wifiLock = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onHandleIntent");
		if (intent == null) {
			Log.e(TAG, "intent is null, exiting.");
			return 0;
		}
		String midiName = intent.getStringExtra("midiFile");
		FileInputStream fis = null;
		try {
			fis = this.openFileInput(midiName);
		} catch (Exception e1) {
			e1.printStackTrace();
			stopSelf();
			Toast.makeText(this, "音乐文件未找到。", 2000).show();
			AVOSLogger.error("File not found." + e1.getMessage());
		}
		AVOSLogger.info("Start MP");
		mp = new MediaPlayer();
		mp.reset();
		try {
			mp.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
					AVOSLogger.error("MP Error " + arg1 + " " + arg2);
					return true;
				}
			});
			mp.setDataSource(fis.getFD());
			mp.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer lmp) {
					mp.setLooping(true);
					mp.setWakeMode(MusicService.this.getApplicationContext(),
							PowerManager.PARTIAL_WAKE_LOCK);
					wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
							.createWifiLock(WifiManager.WIFI_MODE_FULL,
									"mylock");
					wifiLock.acquire();
					mp.start();
					final long startAccr = System.currentTimeMillis();
					timer.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
							final long nowAccr = System.currentTimeMillis();
							// Log.v(TAG, "Current time is " + nowAccr);
							if ((nowAccr - startAccr) >= MusicFragment.musicDuration) { // stop
								stopMusic();
							}
						}

					}, 0, 1000);
				}
			});
			mp.prepareAsync();

		} catch (IOException e) {
			AVOSLogger.error("Music play Error." + e.getMessage());
			Log.e(TAG, "Error playing music", e);
			stopSelf();
			Toast.makeText(this, "无法播放", 15 * 1000).show();
		}
		return START_STICKY;
	}

	private void stopMusic() {
		if (mp != null && mp.isPlaying()) {
			AVOSLogger.info("Stop Music normally.");
			Log.i(TAG, "Stop the player inside the music service.");
			mp.stop();
			timer.cancel();

			wifiLock.release();
		}
	}

}

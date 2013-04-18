package com.duo.midi;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

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
		if (mp != null) {
			mp.stop();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onHandleIntent");
		String urlPath = intent.getStringExtra("midiFile");
		Log.i(TAG, "URL PATH is " + urlPath);
		File tmpMidiFile = new File(urlPath);
		mp = new MediaPlayer();
		try {
			mp.setDataSource(this, Uri.fromFile(tmpMidiFile));
			mp.prepare();
			mp.setLooping(true);
			mp.start();
		} catch (IOException e) {
			Log.e(TAG, "Error playing music", e);
//			Toast.makeText(this, "无法播放", 5 * 1000);
		}
		return START_STICKY;
	}


}

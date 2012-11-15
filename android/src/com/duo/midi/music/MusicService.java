package com.duo.midi.music;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.duo.midi.MainActivity;
import com.duo.midi.MusicFragment;
import com.duo.midi.alarm.WakefulIntentService;

public class MusicService extends WakefulIntentService {
	private static final String TAG = "MusicService";

	public MusicService() {
		super("MusicService");
	}

	@Override
	protected void doWakefulWork(Context context, Intent intent) {
		Log.i(TAG, "I'm awake! I'm awake! (yawn)");
		if(context == null){
			return;
		}
		MainActivity activity = (MainActivity)context;
		MusicFragment mf = activity.getMusicFragment();
		if(mf.isNeedRepeat()){
			mf.getWebView().reload();
		}
	}
}
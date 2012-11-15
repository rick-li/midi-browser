package com.duo.midi.music;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import com.duo.midi.alarm.WakefulIntentService;

public class MusicRepeatListener implements WakefulIntentService.AlarmListener {

	public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context ctxt) {
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				5*1000, 10*1000,
				pi);
		
	}

	public void sendWakefulWork(Context ctxt) {
		WakefulIntentService.sendWakefulWork(ctxt, MusicService.class);
	}

	public long getMaxAge() {
//		return (AlarmManager.INTERVAL_HOUR * 2);
		return (5*1000 * 2);
	}
}
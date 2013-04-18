package com.duo.midi.music;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import com.duo.midi.MusicFragment;
import com.duo.midi.alarm.WakefulIntentService;

public class MusicRepeatListener implements WakefulIntentService.AlarmListener {

	public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context ctxt) {
		// Since this method will be called after the first time music is
		// stopped, the initial waiting would be music wait interval.
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+MusicFragment.waitInterval, pi);
//		mgr.setRepeating(
//				AlarmManager.ELAPSED_REALTIME_WAKEUP,
//				SystemClock.elapsedRealtime()+MusicFragment.waitInterval,
//				(MusicFragment.musicDuration + MusicFragment.waitInterval + MusicFragment.waitAddition),
//				pi);
		
	}

	public void sendWakefulWork(Context ctxt) {
		WakefulIntentService.sendWakefulWork(ctxt, MusicWakeService.class);
	}

	public long getMaxAge() {
		// return (AlarmManager.INTERVAL_HOUR * 2);
		return (MusicFragment.waitInterval);
	}
}
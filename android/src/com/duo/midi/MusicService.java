package com.duo.midi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
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
        if (mp != null) {
            mp.stop();
            timer.cancel();
        }
    }

    Timer timer = new Timer();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onHandleIntent");
        if (intent == null) {
            Log.e(TAG, "intent is null, exiting.");
        }
        String midiName = intent.getStringExtra("midiFile");
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(midiName);
        } catch (Exception e1) {
            e1.printStackTrace();
            stopSelf();
            Toast.makeText(this, "音乐文件未找到。", 2000).show();
        }
        mp = new MediaPlayer();
        try {
            mp.setDataSource(fis.getFD());
            mp.prepare();
            mp.setLooping(true);
            mp.setWakeMode(MusicService.this, PowerManager.PARTIAL_WAKE_LOCK);
            // mp.setWakeMode(this.getBaseContext(),
            // PowerManager.PARTIAL_WAKE_LOCK);
            mp.start();
            final long startAccr = System.currentTimeMillis();
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    final long nowAccr = System.currentTimeMillis();
                    if ((nowAccr - startAccr) >= MusicFragment.musicDuration) { //stop
                        if (mp != null & mp.isPlaying()) {
                            Log.i(TAG,
                                    "Stop the player inside the music service.");
                            mp.stop();
                        }
                    }
                }

            }, 0, 1000);
        } catch (IOException e) {
            Log.e(TAG, "Error playing music", e);
            stopSelf();
            Toast.makeText(this, "无法播放", 5 * 1000);
        }
        return START_STICKY;
    }

}

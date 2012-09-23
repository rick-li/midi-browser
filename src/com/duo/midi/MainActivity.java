package com.duo.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class MainActivity extends Activity {
	private static final String TAG = "midi-browser";

	// private static final String homeUrl = "http://www.duosuccess.com";
	private static final String homeUrl = "http://rick-li.github.com/android-midi/test.html";
	// private String homeUrl = "http://www.baidu.com";
	private String strBaseDir = Environment.getExternalStorageDirectory()
			.getPath() + "/duosuccess";
	private String tmpMidiFile = strBaseDir + "/tmpMid.mid";
	private MediaPlayer mp = new MediaPlayer();
	private WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = (ActionBar) this
				.findViewById(R.id.actionbar);
		actionBar.setHomeAction(new Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_title_home_default;
			}

			@Override
			public void performAction(View view) {
				webView.loadUrl(homeUrl);
			}

		});

		// refresh button
		actionBar.addAction(new Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_refresh;
			}

			@Override
			public void performAction(View view) {
				actionBar.setProgressBarVisibility(View.VISIBLE);
				clearCache();
				webView.reload();
			}
		});

		// close button
		actionBar.addAction(new Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_close_clear_cancel;
			}

			@Override
			public void performAction(View view) {
				stopMedia();
				finish();
			}

		});

		webView = (WebView) this.findViewById(R.id.webView);
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDefaultZoom(ZoomDensity.FAR);
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);

		// auto clear cache.
		clearCache();
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
				Log.i(TAG, "loading url " + url);
				stopMedia();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				stopMedia();
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				webView.loadUrl("javascript:midiExtractor.extract(document.querySelector('embed').src, window.location.href);");
				actionBar.setProgressBarVisibility(View.INVISIBLE);
			}

		});

		class MidiExtractor {
			public void extract(String midiFile, String pageUrl) {
				String midiUrl = midiFile;
				if (!midiFile.startsWith("http")) {
					midiUrl = pageUrl.substring(0, pageUrl.lastIndexOf("/"))
							+ "/" + midiFile;
				}
				Log.i(TAG, "midi is " + midiUrl);
				try {
					URLConnection cn = new URL(midiUrl).openConnection();
					InputStream stream = cn.getInputStream();
					byte[] buffer = new byte[512];
					Log.i(TAG, "Checking baseDir " + strBaseDir);
					File dir = new File(strBaseDir);
					if (!dir.exists()) {
						Log.i(TAG, "base dir " + strBaseDir
								+ " not exist, create new. ");
						dir.mkdir();
					}
					File tmpMidFile = new File(tmpMidiFile);
					tmpMidFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(tmpMidFile);
					int n = -1;
					while ((n = stream.read(buffer)) != -1) {
						fos.write(buffer, 0, n);
					}
					fos.close();
					stream.close();
					mp = new MediaPlayer();
					mp.setDataSource(MainActivity.this,
							Uri.fromFile(tmpMidFile));
					mp.prepare();
					mp.setLooping(true);
					mp.start();

				} catch (Exception e) {
					Log.e(TAG, "unable to play midi. ", e);
				}
			}
		}

		webView.addJavascriptInterface(new MidiExtractor(), "midiExtractor");
		webView.loadUrl(homeUrl);

	}

	private void clearCache() {
		webView.clearCache(true);
		File tmpMidi = new File(tmpMidiFile);
		if (tmpMidi.exists()) {
			boolean status = tmpMidi.delete();
			Log.i(TAG, "TMP midi file removed status: " + status);
		}
	}

	private void stopMedia() {
		if (mp.isPlaying()) {
			mp.stop();
			Log.i(TAG, "media is stopped.");
		}
	}

	@Override
	public void onBackPressed() {
		if (webView != null && webView.canGoBack()) {
			webView.goBack();
		} else {
			super.onBackPressed();
		}
		stopMedia();
	}

}

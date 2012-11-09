package com.duo.midi;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.duo.midi.LocationService.LocationResult;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.actionbar.R;

public class SonarFragment extends Fragment {
	private static final String SONAR_URL = "file:///android_asset/www/sonar.html";
	private static final String TAG = "duosuccess-sonar";
	private WebView sonarWebView;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sonar, null);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		sonarWebView = (WebView) view.findViewById(R.id.sonarWebView);
		ActionBar actionBar = (ActionBar) view
				.findViewById(R.id.sonarActionbar);
		actionBar.addAction(new Action() {
			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_refresh;
			}

			@Override
			public void performAction(View view) {
				sonarWebView.loadUrl(SONAR_URL);
			}
		});
		WebSettings settings = sonarWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		WebView.enablePlatformNotifications();
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setGeolocationEnabled(true);

		sonarWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				new LocationService().getLocation(getApplicationContext(),
						new LocationResult() {

							@Override
							public void gotLocation(Location loc) {
								pd.dismiss();
								Log.i(TAG,
										"javascript:initPosition({'coords':{'latitude':"
												+ loc.getLatitude()
												+ ",'longitude':"
												+ loc.getLongitude() + "}});");
								sonarWebView
										.loadUrl("javascript:initPosition({'coords':{'latitude':"
												+ loc.getLatitude()
												+ ",'longitude':"
												+ loc.getLongitude() + "}});");
							}

						});
				pd = ProgressDialog.show(SonarFragment.this.getActivity(), "",
						"正在獲取位置, 請稍候...");

			}
		});
		sonarWebView.loadUrl(SONAR_URL);

	}

	public Context getApplicationContext() {
		return this.getActivity().getApplicationContext();
	}
}

package com.duo.midi;

import java.util.List;
import java.util.Map;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.duosuccess.midi.R;
import com.google.common.collect.ImmutableMap;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "Main";
	private boolean isMusicPageSelected = true;
	MusicFragment musicFragment = null;
	SonarFragment sonarFragment = null;
	TabPageIndicator indicator = null;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		// throw an exception to test feature.
		// String a = null;
		// a.toString();

		Parse.initialize(this, "L8yb6OqqvqHZhwViBea5xCWAgtRtow0R3CtDjz1E",
				"KmDVN8meSLM9QjYE5wsrbVMtmWpbLo0MYxpAFFCR");
		try {
			final String installationId = ParseInstallation
					.getCurrentInstallation().getInstallationId();
			ParseAnalytics.trackAppOpened(this.getIntent());

			final Map<String, String> logDetail = ImmutableMap
					.<String, String> builder()
					.put("installId", installationId)
					.put("appVersion", getAppVersion())
					.put("androidVersion", android.os.Build.VERSION.RELEASE)
					.build();
			ParseAnalytics.trackEvent("App Open", logDetail);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery("AndroidInstallation");
			query.whereEqualTo("installationId", ParseInstallation
					.getCurrentInstallation().getInstallationId());
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> results, ParseException e) {
					ParseObject installObj;
					if (results == null || results.size() <= 0) {
						installObj = new ParseObject("AndroidInstallation");
						installObj.put("installationId", installationId);
					} else {
						installObj = results.get(0);
					}
					installObj.put("appVersion", getAppVersion());
					installObj.put("androidVersion",
							android.os.Build.VERSION.RELEASE);
					installObj.saveInBackground();
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "Fail to start with parse.");
		}
		if (Build.VERSION.SDK_INT < 17) {
			try {

				Settings.System.putInt(getContentResolver(),
						Settings.System.WIFI_SLEEP_POLICY,
						Settings.System.WIFI_SLEEP_POLICY_NEVER);

			} catch (Exception e) {
				Log.w(TAG, "Unable to set sleep policy.", e);
			}
		}

		this.setContentView(R.layout.main);
		FragmentStatePagerAdapter adapter = new DuosuccessAdapter(
				getSupportFragmentManager());

		this.musicFragment = new MusicFragment();
		this.musicFragment.setRetainInstance(true);
		this.sonarFragment = new SonarFragment();
		this.sonarFragment.setRetainInstance(true);

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		this.indicator = (TabPageIndicator) findViewById(R.id.indicator);

		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int pageNo) {
				if (pageNo == 0) {
					isMusicPageSelected = true;
				} else {
					sonarFragment.startLocationService();
					isMusicPageSelected = false;
				}
			}
		});

		// check version.
		try {
			new UpgradeChecker(this).execute();
		} catch (Exception e) {

		}

	}

	private String getAppVersion() {
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			return pInfo.versionName;

		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		return "0";
	}

	class DuosuccessAdapter extends FragmentStatePagerAdapter {
		public DuosuccessAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			if (position == 0) {
				return musicFragment;
			} else {
				return sonarFragment;
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {

			if (position == 0) {
				return "多成音樂";
			} else {
				return "真太陽時";
			}

		}

		@Override
		public int getCount() {
			return 2;
		}
	}

	@Override
	public void onBackPressed() {
		Log.i(TAG, "Main back pressed.");

		if (isMusicPageSelected) {
			boolean handled = this.musicFragment.onBackPressed();
			if (!handled) {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}

	public MusicFragment getMusicFragment() {
		return musicFragment;
	}

	public void setMusicFragment(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
	}

	public TabPageIndicator getIndicator() {
		return indicator;
	}

	public void setIndicator(TabPageIndicator indicator) {
		this.indicator = indicator;
	}

}

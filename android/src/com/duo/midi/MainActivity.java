package com.duo.midi;

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
		Settings.System.putInt(getContentResolver(),
				Settings.System.WIFI_SLEEP_POLICY,
				Settings.System.WIFI_SLEEP_POLICY_NEVER);

		this.setContentView(R.layout.main);
		FragmentStatePagerAdapter adapter = new DuosuccessAdapter(
				getSupportFragmentManager());

		this.musicFragment = new MusicFragment();
		this.sonarFragment = new SonarFragment();

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

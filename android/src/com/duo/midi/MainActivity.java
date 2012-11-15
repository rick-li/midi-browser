package com.duo.midi;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity {
	private boolean isMusicPageSelected = false;
	MusicFragment musicFragment = null;
	SonarFragment sonarFragment = null;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Settings.System.putInt(getContentResolver(),
				  Settings.System.WIFI_SLEEP_POLICY, 
				  Settings.System.WIFI_SLEEP_POLICY_NEVER);
		
		this.setContentView(com.duo.midi.R.layout.main);
		FragmentStatePagerAdapter adapter = new DuosuccessAdapter(
				getSupportFragmentManager());

		this.musicFragment = new MusicFragment();
		this.sonarFragment = new SonarFragment();

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
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
				return "¶à³ÉÒô˜·";
			} else {
				return "ÕæÌ«ê–•r";
			}

		}

		@Override
		public int getCount() {
			return 2;
		}
	}

	@Override
	public void onBackPressed() {
		if (isMusicPageSelected) {
			this.musicFragment.onBackPressed();
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



}

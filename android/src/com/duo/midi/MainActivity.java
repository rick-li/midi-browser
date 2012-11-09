package com.duo.midi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(com.duo.midi.R.layout.main);
		FragmentStatePagerAdapter adapter = new DuosuccessAdapter(
				getSupportFragmentManager());

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
	}

	class DuosuccessAdapter extends FragmentStatePagerAdapter {
		public DuosuccessAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			if (position == 0) {
				return new MusicFragment();
			} else {
				return new SonarFragment();
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
		// if (webView != null && webView.canGoBack()) {
		// webView.goBack();
		// } else {
		// super.onBackPressed();
		// }
		// stopMedia();
	}

}

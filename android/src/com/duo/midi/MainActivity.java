package com.duo.midi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "Main";
	private boolean isMusicPageSelected = true;
	SonarFragment sonarFragment = null;
	TabPageIndicator indicator = null;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		this.sonarFragment = new SonarFragment();
		FrameLayout frame = new FrameLayout(this);
		int CONTENT_VIEW_ID = 10101010;

		frame.setId(CONTENT_VIEW_ID);
		setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		Fragment newFragment = new SonarFragment();
		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		ft.add(CONTENT_VIEW_ID, newFragment).commit();

	}

}

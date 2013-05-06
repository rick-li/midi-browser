package com.duo.midi;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipePager extends ViewPager {

	public NonSwipePager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public NonSwipePager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Never allow swiping to switch between pages
		return false;
	}

}

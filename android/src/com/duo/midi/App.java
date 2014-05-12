package com.duo.midi;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.util.Log;

@ReportsCrashes(formKey = "", formUri = "https://racke1983cn.cloudant.com/acra-myapp/_design/acra-storage/_update/report", reportType = org.acra.sender.HttpSender.Type.JSON, httpMethod = org.acra.sender.HttpSender.Method.PUT, formUriBasicAuthLogin = "acknowerefinguedichiment", formUriBasicAuthPassword = "5sdXJDxoluab4HMVx11Y6QvN"

)
public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("APP", "App oncreate");

		ACRA.init(this);

	}

}

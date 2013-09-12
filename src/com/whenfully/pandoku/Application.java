package com.whenfully.pandoku;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class Application extends android.app.Application {
	private static final String TAG = Application.class.getName();

	@Override
	public void onCreate() {
		super.onCreate();

		provideDefaultValueForFullscreenMode();

		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
	}

	private void provideDefaultValueForFullscreenMode() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.contains(Settings.KEY_FULLSCREEN_MODE)) {
			Log.d(TAG, "Fullscreen mode has already been set");
		}
		else {
			Log.d(TAG, "Fullscreen mode undecided");
			Editor editor = preferences.edit();
			editor.putBoolean(Settings.KEY_FULLSCREEN_MODE, isDefaultFullscreenMode());
			editor.commit();
		}
	}

	private boolean isDefaultFullscreenMode() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float aspect = (float) metrics.widthPixels / metrics.heightPixels;
		boolean fullscreen = aspect >= 320f / 480;

		Log.d(TAG, "Width: " + metrics.widthPixels + ", height: " + metrics.heightPixels
				+ ", aspect: " + aspect + ", fullscreen: " + fullscreen);

		return fullscreen;
	}
}

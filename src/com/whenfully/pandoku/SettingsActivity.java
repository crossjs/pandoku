package com.whenfully.pandoku;

import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		final ListView listView = getListView();
		listView.setBackgroundResource(R.drawable.bg);
		listView.setCacheColorHint(0);

		setContentView(R.layout.settings);

		((TextView) findViewById(R.id.pageTitle)).setTypeface(Typeface
				.createFromAsset(getAssets(), "fonts/pandoku.ttf"));
	}
}

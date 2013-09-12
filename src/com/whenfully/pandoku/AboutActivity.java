package com.whenfully.pandoku;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutActivity extends Activity {
	private static final String TAG = AboutActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		Typeface typeface = Typeface.createFromAsset(getAssets(),
				"fonts/pandoku.ttf");

		((TextView) findViewById(R.id.pageTitle)).setTypeface(typeface);
		((TextView) findViewById(R.id.pageButton)).setTypeface(typeface);

		findViewById(R.id.pageButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}

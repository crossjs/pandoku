package com.whenfully.pandoku;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Gallery.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.whenfully.pandoku.db.PandokuDatabase;
import com.whenfully.pandoku.db.GameStatistics;
import com.whenfully.pandoku.source.PuzzleSourceIds;

public class NewGameActivity extends Activity {
	private static final String TAG = NewGameActivity.class.getName();

	private static final String PREF_KEY_PUZZLE_STYLE = "puzzleStyle";
	private static final String PREF_KEY_PUZZLE_LEVEL = "puzzleLevel";

	private PandokuDatabase db;

	private Gallery styleGallery;
	private SeekBar levelSeekBar;
	private TextView miniStats;

	private TextView[] levelTexts;

	private Integer[] mThumbIds = { R.drawable.b_standard_n,
			R.drawable.b_standard_x, R.drawable.b_standard_h,
			R.drawable.b_standard_p, R.drawable.b_standard_c,
			R.drawable.b_squiggly_n, R.drawable.b_squiggly_x,
			R.drawable.b_squiggly_h, R.drawable.b_squiggly_p, R.drawable.b_squiggly_c };

	private String[] styleStrings = { "standard_n_", "standard_x_",
			"standard_h_", "standard_p_", "standard_c_", "squiggly_n_",
			"squiggly_x_", "squiggly_h_", "squiggly_p_", "squiggly_c_" };

	private String selectedPuzzleSourceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.newgame);
        MyBroadcastReceiver.register(this);

		db = new PandokuDatabase(this);

		Typeface typeface = Typeface.createFromAsset(getAssets(),
				"fonts/pandoku.ttf");

		((TextView) findViewById(R.id.pageTitle)).setTypeface(typeface);
		((TextView) findViewById(R.id.pageButton)).setTypeface(typeface);

		styleGallery = (Gallery) findViewById(R.id.styleGallery);

		styleGallery.setAdapter(new ImageAdapter(this));

		styleGallery
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id) {
						onSelectionChanged();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		Button pageButton = (Button) findViewById(R.id.pageButton);
		pageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onPageButton();
			}
		});

		levelTexts = new TextView[] { (TextView) findViewById(R.id.levelText1),
				(TextView) findViewById(R.id.levelText2),
				(TextView) findViewById(R.id.levelText3),
				(TextView) findViewById(R.id.levelText4),
				(TextView) findViewById(R.id.levelText5) };

		levelSeekBar = (SeekBar) findViewById(R.id.levelSeekBar);

		final OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				onSelectionChanged();
				for (int i = 0; i < levelTexts.length; i++) {
					levelTexts[i].setTextColor(getResources().getColor(
							i == progress ? R.color.p_blue : R.color.p_text));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		};

		levelSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

		miniStats = (TextView) findViewById(R.id.miniStats);

		loadPuzzlePreferences();
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		if (db != null) {
			db.close();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateMiniStats(getSelectedPuzzleSource());
	}

	void onSelectionChanged() {
		String puzzleSourceId = getSelectedPuzzleSource();
		if (selectedPuzzleSourceId == null
				|| !selectedPuzzleSourceId.equals(puzzleSourceId)) {
			updateMiniStats(puzzleSourceId);
		}
	}

	private void updateMiniStats(String puzzleSourceId) {
		selectedPuzzleSourceId = puzzleSourceId;

		GameStatistics statistics = db.getStatistics(puzzleSourceId);
		int solved = statistics.numGamesSolved;

		final Resources resources = getResources();
		if (solved == 0) {
			miniStats.setText(resources.getString(R.string.mini_stats_0));
		} else {
			String averageTime = DateUtil.formatTime(statistics.getAverageTime());
			String fastestTime = DateUtil.formatTime(statistics.minTime);
			miniStats.setText(resources.getString(R.string.mini_stats_n, solved,
					averageTime, fastestTime));
		}
	}

	void onPageButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onPageButton()");

		savePuzzlePreferences();

		String puzzleSourceId = getSelectedPuzzleSource();

		new GameLauncher(this, db).startNewGame(puzzleSourceId);
	}

	private void loadPuzzlePreferences() {
		if (Constants.LOG_V)
			Log.v(TAG, "loadPuzzlePreferences()");

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);

		styleGallery.setSelection(preferences.getInt(PREF_KEY_PUZZLE_STYLE, 0));
		levelSeekBar.setProgress(preferences.getInt(PREF_KEY_PUZZLE_LEVEL, 2));
	}

	private void savePuzzlePreferences() {
		if (Constants.LOG_V)
			Log.v(TAG, "savePuzzlePreferences()");

		Editor editor = getPreferences(MODE_PRIVATE).edit();

		editor
				.putInt(PREF_KEY_PUZZLE_STYLE, styleGallery.getSelectedItemPosition());
		editor.putInt(PREF_KEY_PUZZLE_LEVEL, levelSeekBar.getProgress());

		editor.commit();
	}

	private String getSelectedPuzzleSource() {
		String folderName = getSelectedAssetFolderName();
		return PuzzleSourceIds.forAssetFolder(folderName);
	}

	private String getSelectedAssetFolderName() {
		StringBuilder sb = new StringBuilder();

		sb.append(styleStrings[styleGallery.getSelectedItemPosition()]);

		int level = levelSeekBar.getProgress() + 1;
		if (level < 1 || level > 5)
			throw new IllegalStateException();

		sb.append(level);

		return sb.toString();
	}

	public class ImageAdapter extends BaseAdapter {

		private Context mContext;

		public ImageAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return mThumbIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView image = new ImageView(mContext);

			image.setImageResource(mThumbIds[position]);
			image.setAdjustViewBounds(true);
			image.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));

			return image;
		}
	}
}

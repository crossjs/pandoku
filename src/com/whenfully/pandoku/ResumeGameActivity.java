package com.whenfully.pandoku;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.whenfully.pandoku.db.PandokuDatabase;
import com.whenfully.pandoku.db.PuzzleId;
import com.whenfully.pandoku.model.PuzzleType;

public class ResumeGameActivity extends ListActivity {
	private static final String TAG = ResumeGameActivity.class.getName();

	private PandokuDatabase db;

	private long baseTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.resume);

		Typeface typeface = Typeface.createFromAsset(getAssets(),
				"fonts/pandoku.ttf");
		((TextView) findViewById(R.id.pageTitle)).setTypeface(typeface);
		((TextView) findViewById(R.id.pageButton)).setTypeface(typeface);

		db = new PandokuDatabase(this);

		Cursor cursor = db.findGamesInProgress();
		startManagingCursor(cursor);

		String[] from = { PandokuDatabase.COL_TYPE, PandokuDatabase.COL_SOURCE,
				PandokuDatabase.COL_TYPE, PandokuDatabase.COL_TIMER,
				PandokuDatabase.COL_MODIFIED_DATE };
		int[] to = { R.id.save_game_icon, R.id.save_game_difficulty,
				R.id.save_game_title, R.id.save_game_timer,
				R.id.save_game_modified };
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
				R.layout.save_game_list_item, cursor, from, to);
		listAdapter.setViewBinder(new SaveGameViewBinder(getResources()));
		setListAdapter(listAdapter);

		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						onResumeGameItem(id);
					}
				});

		findViewById(R.id.pageButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume()");

		super.onResume();

		baseTime = System.currentTimeMillis();

		final boolean hasSavedGames = getListAdapter().getCount() != 0;
		if (!hasSavedGames)
			finish();
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

	void onResumeGameItem(long rowId) {
		if (Constants.LOG_V)
			Log.v(TAG, "onResumeGameItem(" + rowId + ")");

		PuzzleId puzzleId = db.puzzleIdByRowId(rowId);

		Intent intent = new Intent(this, PandokuActivity.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID,
				puzzleId.puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, puzzleId.number);
		intent.putExtra(Constants.EXTRA_START_PUZZLE, true);
		startActivity(intent);
	}

	private final class SaveGameViewBinder implements
			SimpleCursorAdapter.ViewBinder {
		private static final int IDX_SOURCE = PandokuDatabase.IDX_GAME_SOURCE;
		private static final int IDX_NUMBER = PandokuDatabase.IDX_GAME_NUMBER;
		private static final int IDX_TYPE = PandokuDatabase.IDX_GAME_TYPE;
		private static final int IDX_TIMER = PandokuDatabase.IDX_GAME_TIMER;
		private static final int IDX_DATE_MODIFIED = PandokuDatabase.IDX_GAME_MODIFIED_DATE;

		public SaveGameViewBinder(Resources resources) {
		}

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view instanceof ImageView) {
				assert columnIndex == IDX_TYPE;
				PuzzleType puzzleType = PuzzleType.forOrdinal(cursor
						.getInt(IDX_TYPE));
				Drawable drawable = Util.getPuzzleIcon(getResources(),
						puzzleType);
				((ImageView) view).setImageDrawable(drawable);
				return true;
			}

			if (!(view instanceof TextView))
				return false;

			TextView textView = (TextView) view;

			switch (columnIndex) {
			case IDX_TYPE:
				PuzzleType puzzleType = PuzzleType.forOrdinal(cursor
						.getInt(columnIndex));
				String name = Util.getPuzzleName(getResources(), puzzleType);
				textView.setText(name);
				return true;

			case IDX_TIMER:
				String time = DateUtil.formatTime(cursor.getLong(columnIndex));
				textView.setText(time);
				return true;

			case IDX_DATE_MODIFIED:
				String age = DateUtil.formatTimeSpan(getResources(), baseTime,
						cursor.getLong(columnIndex));
				textView.setText(age);
				return true;

			case IDX_SOURCE:
				String difficultyAndNumber = parseLevelAndNumber(
						cursor.getString(IDX_SOURCE), cursor.getInt(IDX_NUMBER));
				textView.setText(difficultyAndNumber);
				return true;
			}

			return false;
		}

		private String parseLevelAndNumber(String sourceId, int number) {
			String[] difficulties = getResources().getStringArray(
					R.array.levels);
			int difficulty = sourceId.charAt(sourceId.length() - 1) - '0' - 1;
			return difficulties[difficulty] + " #" + (number + 1);
		}
	}
}

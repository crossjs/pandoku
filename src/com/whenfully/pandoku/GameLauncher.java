package com.whenfully.pandoku;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.whenfully.pandoku.db.PandokuDatabase;
import com.whenfully.pandoku.source.PuzzleSource;
import com.whenfully.pandoku.source.PuzzleSourceResolver;

class GameLauncher {
	private static final String TAG = GameLauncher.class.getName();

	private final Context context;
	private final PandokuDatabase db;

	public GameLauncher(Context context, PandokuDatabase db) {
		this.context = context;
		this.db = db;
	}

	public void startNewGame(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "startNewGame(" + puzzleSourceId + ")");

		int number = findAvailableGame(puzzleSourceId);

		startGame(puzzleSourceId, number);
	}

	private int findAvailableGame(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "findAvailableGame(" + puzzleSourceId + ")");

		int numberOfPuzzles = getNumberOfPuzzles(puzzleSourceId);

		int candidate = 0;
		int fallback = -1; // use an unsolved game if no unplayed game found

		Cursor c = db.findGamesBySource(puzzleSourceId);

		try {
			while (c.moveToNext()) {
				int number = c.getInt(PandokuDatabase.IDX_GAME_BY_SOURCE_NUMBER);

				// is there a gap and candidate number is available?
				if (number > candidate) {
					if (Constants.LOG_V)
						Log.v(TAG, "found gap before save game " + number + "; returning " + candidate);

					return candidate;
				}

				boolean solved = c.getInt(PandokuDatabase.IDX_GAME_BY_SOURCE_SOLVED) != 0;
				if (!solved && fallback == -1)
					fallback = number;

				candidate++;
			}

			if (puzzleExists(candidate, numberOfPuzzles)) {
				if (Constants.LOG_V)
					Log.v(TAG, "returning next game after save games: " + candidate);

				return candidate;
			}

			if (fallback != -1 && puzzleExists(fallback, numberOfPuzzles)) {
				if (Constants.LOG_V)
					Log.v(TAG, "all games played; returning first uncomplete: " + fallback);

				return fallback;
			}

			if (Constants.LOG_V)
				Log.v(TAG, "all games solved; returning 0");

			return 0;
		}
		finally {
			c.close();
		}
	}

	private int getNumberOfPuzzles(String puzzleSourceId) {
		PuzzleSource puzzleSource = PuzzleSourceResolver.resolveSource(context, puzzleSourceId);
		try {
			return puzzleSource.numberOfPuzzles();
		}
		finally {
			puzzleSource.close();
		}
	}

	private boolean puzzleExists(int number, int total) {
		return number >= 0 && number < total;
	}

	private void startGame(String puzzleSourceId, int number) {
		Intent intent = new Intent(context, PandokuActivity.class);
		intent.putExtra(Constants.EXTRA_PUZZLE_SOURCE_ID, puzzleSourceId);
		intent.putExtra(Constants.EXTRA_PUZZLE_NUMBER, number);
		context.startActivity(intent);
	}
}

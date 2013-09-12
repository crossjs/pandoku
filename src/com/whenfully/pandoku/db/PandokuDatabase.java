package com.whenfully.pandoku.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.whenfully.pandoku.Constants;
import com.whenfully.pandoku.TickTimer;
import com.whenfully.pandoku.model.PandokuPuzzle;

public class PandokuDatabase {
	private static final String TAG = PandokuDatabase.class.getName();

	public static final String DATABASE_NAME = "save_games.db";
	private static final int DATABASE_VERSION = 2;

	public static final String COL_ID = BaseColumns._ID;

	public static final String TABLE_PUZZLES = "puzzles";
	public static final String COL_NAME = "name";
	public static final String COL_DIFFICULTY = "difficulty"; // 0-4|-1
	public static final String COL_SIZE = "size"; //             9
	public static final String COL_CLUES = "clues"; //           "...6.12........3......"
	public static final String COL_AREAS = "areas"; //           "11122223311122222341.."|""
	public static final String COL_EXTRA_REGIONS = "extra"; //   "X"|"H"|"P"|"C"|""

	private static final String TABLE_GAMES = "games";
	public static final String COL_SOURCE = "source";
	public static final String COL_NUMBER = "number";
	public static final String COL_TYPE = "type";
	public static final String COL_PUZZLE = "puzzle";
	public static final String COL_TIMER = "timer";
	public static final String COL_SOLVED = "solved";
	public static final String COL_CREATED_DATE = "created";
	public static final String COL_MODIFIED_DATE = "modified";

	// indexes for findAllGames() and findGamesInProgress();
	public static final int IDX_GAME_ID = 0;
	public static final int IDX_GAME_SOURCE = 1;
	public static final int IDX_GAME_NUMBER = 2;
	public static final int IDX_GAME_TYPE = 3;
	public static final int IDX_GAME_TIMER = 4;
	public static final int IDX_GAME_CREATED_DATE = 5;
	public static final int IDX_GAME_MODIFIED_DATE = 6;

	// indexes for findGamesBySource()
	public static final int IDX_GAME_BY_SOURCE_NUMBER = 0;
	public static final int IDX_GAME_BY_SOURCE_SOLVED = 1;

	private DatabaseHelper openHelper;

	public PandokuDatabase(Context context) {
		if (Constants.LOG_V)
			Log.v(TAG, "PandokuDatabase()");

		openHelper = new DatabaseHelper(context);
	}

	public void resetAll() {
		if (Constants.LOG_V)
			Log.v(TAG, "resetAll()");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.delete(TABLE_GAMES, null, null);
	}

	public void saveGame(PuzzleId puzzleId, PandokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "saveGame(" + puzzleId + ")");

		long now = System.currentTimeMillis();

		SQLiteDatabase db = openHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			String[] columns = { COL_ID };
			String selection = COL_SOURCE + "=? AND " + COL_NUMBER + "=?";
			String[] selectionArgs = { puzzleId.puzzleSourceId, String.valueOf(puzzleId.number) };
			Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);

			long rowId = -1;
			if (cursor.moveToFirst()) {
				rowId = cursor.getLong(0);
			}

			cursor.close();

			ContentValues values = new ContentValues();
			values.put(COL_PUZZLE, puzzle.saveToMemento());
			values.put(COL_TIMER, timer.getTime());
			values.put(COL_SOLVED, puzzle.isSolved());
			values.put(COL_MODIFIED_DATE, now);

			if (rowId == -1) {
				values.put(COL_SOURCE, puzzleId.puzzleSourceId);
				values.put(COL_NUMBER, puzzleId.number);
				values.put(COL_TYPE, puzzle.getPuzzleType().ordinal());
				values.put(COL_CREATED_DATE, now);
				long insertedRowId = db.insert(TABLE_GAMES, null, values);
				if (insertedRowId == -1)
					return;
			}
			else {
				int updated = db.update(TABLE_GAMES, values, COL_ID + "=?", new String[] { String
						.valueOf(rowId) });
				if (updated == 0)
					return;
			}

			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	public boolean loadGame(PuzzleId puzzleId, PandokuPuzzle puzzle, TickTimer timer) {
		if (Constants.LOG_V)
			Log.v(TAG, "loadGame(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_PUZZLE, COL_TIMER };
		String selection = COL_SOURCE + "=? AND " + COL_NUMBER + "=?";
		String[] selectionArgs = { puzzleId.puzzleSourceId, String.valueOf(puzzleId.number) };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return false;
			}

			byte[] memento = cursor.getBlob(0);
			long time = cursor.getLong(1);

			if (!puzzle.restoreFromMemento(memento)) {
				Log.w(TAG, "Could not restore puzzle memento for " + puzzleId);
				return false;
			}

			timer.setTime(time);
			return true;
		}
		finally {
			cursor.close();
		}
	}

	public void delete(PuzzleId puzzleId) {
		if (Constants.LOG_V)
			Log.v(TAG, "delete(" + puzzleId + ")");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		String whereClause = COL_SOURCE + "=? AND " + COL_NUMBER + "=?";
		String[] whereArgs = { puzzleId.puzzleSourceId, String.valueOf(puzzleId.number) };
		db.delete(TABLE_GAMES, whereClause, whereArgs);
	}

	public void deleteAll(String sourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "deleteAll(" + sourceId + ")");

		SQLiteDatabase db = openHelper.getWritableDatabase();

		String whereClause = COL_SOURCE + "=?";
		String[] whereArgs = { sourceId };
		db.delete(TABLE_GAMES, whereClause, whereArgs);
	}

	public Cursor findAllGames() {
		if (Constants.LOG_V)
			Log.v(TAG, "findAllGames()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_ID, COL_SOURCE, COL_NUMBER, COL_TYPE, COL_TIMER, COL_CREATED_DATE,
				COL_MODIFIED_DATE };
		return db.query(TABLE_GAMES, columns, null, null, null, null, null);
	}

	public boolean hasGamesInProgress() {
		Cursor cursor = findGamesInProgress();
		try {
			return cursor.moveToNext();
		}
		finally {
			cursor.close();
		}
	}

	public Cursor findGamesInProgress() {
		if (Constants.LOG_V)
			Log.v(TAG, "findGamesInProgress()");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_ID, COL_SOURCE, COL_NUMBER, COL_TYPE, COL_TIMER, COL_CREATED_DATE,
				COL_MODIFIED_DATE };
		String selection = COL_SOLVED + "=0";
		String orderBy = COL_MODIFIED_DATE + " DESC";
		return db.query(TABLE_GAMES, columns, selection, null, null, null, orderBy);
	}

	public Cursor findGamesBySource(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "findGamesBySource(" + puzzleSourceId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_NUMBER, COL_SOLVED };
		String selection = COL_SOURCE + "=?";
		String[] selectionArgs = new String[] { puzzleSourceId };
		String orderBy = COL_NUMBER;
		return db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, orderBy);
	}

	public GameStatistics getStatistics(String puzzleSourceId) {
		if (Constants.LOG_V)
			Log.v(TAG, "getStatistics(" + puzzleSourceId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { "COUNT(*)", "SUM(timer)", "MIN(timer)", "MAX(timer)" };
		String selection = COL_SOURCE + "=? AND " + COL_SOLVED + "=1";
		String[] selectionArgs = new String[] { puzzleSourceId };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			cursor.moveToFirst();

			return new GameStatistics(cursor.getInt(0), cursor.getLong(1), cursor.getLong(2));
		}
		finally {
			cursor.close();
		}
	}

	public PuzzleId puzzleIdByRowId(long rowId) {
		if (Constants.LOG_V)
			Log.v(TAG, "puzzleIdByRowId(" + rowId + ")");

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String[] columns = { COL_SOURCE, COL_NUMBER };
		String selection = COL_ID + "=?";
		String[] selectionArgs = { Long.toString(rowId) };
		Cursor cursor = db.query(TABLE_GAMES, columns, selection, selectionArgs, null, null, null);
		try {
			if (!cursor.moveToFirst()) {
				return null;
			}

			String puzzleSourceId = cursor.getString(0);
			int number = cursor.getInt(1);
			return new PuzzleId(puzzleSourceId, number);
		}
		finally {
			cursor.close();
		}
	}

	public void close() {
		if (Constants.LOG_V)
			Log.v(TAG, "close()");

		openHelper.close();
	}

	public void beginTransaction() {
		openHelper.getWritableDatabase().beginTransaction();
	}

	public void endTransaction() {
		openHelper.getWritableDatabase().endTransaction();
	}

	public void setTransactionSuccessful() {
		openHelper.getWritableDatabase().setTransactionSuccessful();
	}

	public Cursor query(SQLiteQueryBuilder qb, String[] projection, String selection,
			String[] selectionArgs, String orderBy) {
		SQLiteDatabase db = openHelper.getReadableDatabase();

		return qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE " + TABLE_GAMES + " (" + COL_ID + " INTEGER PRIMARY KEY,"
					+ COL_SOURCE + " TEXT," + COL_NUMBER + " INTEGER," + COL_TYPE + " INTEGER,"
					+ COL_PUZZLE + " BLOB," + COL_TIMER + " INTEGER," + COL_SOLVED + " BOOLEAN,"
					+ COL_CREATED_DATE + " INTEGER," + COL_MODIFIED_DATE + " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ".");

			db.beginTransaction();
			try {
				if (oldVersion < 2)
					upgradeV1ToV2(db);

				db.setTransactionSuccessful();
			}
			finally {
				db.endTransaction();
			}
		}

		private void upgradeV1ToV2(SQLiteDatabase db) {
			Log.d(TAG, "Upgrading from version 1 to 2.");

			// Equivalent to "ALTER TABLE games DROP COLUMN pid;" which is not supported by sqlite.
			// Loses the ID column but that does not matter.

			db.execSQL("ALTER TABLE " + TABLE_GAMES + " RENAME TO tmp;");
			onCreate(db);

			Cursor cursor = db.query("tmp", null, null, null, null, null, null);
			while (cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				// idx 0 is ID
				// idx 1 is old puzzleId ("pid") to be removed
				values.put(COL_SOURCE, cursor.getString(2));
				values.put(COL_NUMBER, cursor.getInt(3));
				values.put(COL_TYPE, cursor.getInt(4));
				values.put(COL_PUZZLE, cursor.getBlob(5));
				values.put(COL_TIMER, cursor.getLong(6));
				values.put(COL_SOLVED, cursor.getInt(7));
				values.put(COL_CREATED_DATE, cursor.getLong(8));
				values.put(COL_MODIFIED_DATE, cursor.getLong(9));

				db.insert(TABLE_GAMES, null, values);
			}
			cursor.close();

			db.execSQL("DROP TABLE tmp;");

			Log.d(TAG, "Upgraded from version 1 to 2.");
		}
	}
}

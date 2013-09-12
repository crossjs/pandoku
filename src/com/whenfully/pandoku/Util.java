package com.whenfully.pandoku;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.whenfully.pandoku.model.PuzzleType;

class Util {
	private Util() {
	}

	/*
	 * 判断是否已装载SD卡（？）
	 */
	public static boolean isSDCardExist() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}
	
	/*
	 * 返回是否载入SD卡（？）
	 */
	public static String getSDCardPath() {
		File sdDir = null;
		if (isSDCardExist()) {
			sdDir = Environment.getExternalStorageDirectory();//获得SD卡根目录
		}
		return sdDir.toString();
	}

	public static void exit(Context context) {
		Intent intent = new Intent();
		intent.setAction(Constants.PACKAGE_NAME + ".exit");
		context.sendBroadcast(intent);
	}

	public static int dipInt(Context context, int value) {
		return Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, value, context.getResources()
						.getDisplayMetrics()));
	}

	public static int[] colorRing(int color, int nColors) {
		return colorRing(color, nColors, 360f / nColors);
	}

	public static int[] colorRing(int color, int nColors, float hueIncrement) {
		if (nColors < 2)
			throw new IllegalArgumentException();

		int alpha = Color.alpha(color);
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);

		int[] colors = new int[nColors];

		for (int i = 0; i < nColors; i++) {
			colors[i] = Color.HSVToColor(alpha, hsv);

			hsv[0] += hueIncrement;
			if (hsv[0] >= 360f)
				hsv[0] -= 360f;
		}

		return colors;
	}

	public static void setFullscreenMode(Activity activity) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(activity);
		boolean fullscreenMode = settings.getBoolean(
				Settings.KEY_FULLSCREEN_MODE, true);
		if (fullscreenMode) {
			// FEATURE_PROGRESS seems to suppress the tiny drop shadow at the
			// top of the screen
			activity.requestWindowFeature(Window.FEATURE_PROGRESS);

			final Window window = activity.getWindow();

			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			// Workaround for issue #1
			// FLAG_LAYOUT_NO_LIMITS: allow window to extend outside of the
			// screen.
			window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	public static String getPuzzleName(Resources resources,
			PuzzleType puzzleType) {
		return resources.getString(getNameResourceId(puzzleType));
	}

	public static Drawable getPuzzleIcon(Resources resources,
			PuzzleType puzzleType) {
		return resources.getDrawable(getIconResourceId(puzzleType));
	}

	private static int getNameResourceId(PuzzleType puzzleType) {
		switch (puzzleType) {
		case STANDARD:
			return R.string.name_sudoku_standard;
		case STANDARD_X:
			return R.string.name_sudoku_standard_x;
		case STANDARD_HYPER:
			return R.string.name_sudoku_standard_hyper;
		case STANDARD_PERCENT:
			return R.string.name_sudoku_standard_percent;
		case STANDARD_COLOR:
			return R.string.name_sudoku_standard_color;
		case SQUIGGLY:
			return R.string.name_sudoku_squiggly;
		case SQUIGGLY_X:
			return R.string.name_sudoku_squiggly_x;
		case SQUIGGLY_HYPER:
			return R.string.name_sudoku_squiggly_hyper;
		case SQUIGGLY_PERCENT:
			return R.string.name_sudoku_squiggly_percent;
		case SQUIGGLY_COLOR:
			return R.string.name_sudoku_squiggly_color;
		}
		throw new IllegalStateException();
	}

	private static int getIconResourceId(PuzzleType puzzleType) {
		switch (puzzleType) {
		case STANDARD:
			return R.drawable.standard_n;
		case STANDARD_X:
			return R.drawable.standard_x;
		case STANDARD_HYPER:
			return R.drawable.standard_h;
		case STANDARD_PERCENT:
			return R.drawable.standard_p;
		case STANDARD_COLOR:
			return R.drawable.standard_c;
		case SQUIGGLY:
			return R.drawable.squiggly_n;
		case SQUIGGLY_X:
			return R.drawable.squiggly_x;
		case SQUIGGLY_HYPER:
			return R.drawable.squiggly_h;
		case SQUIGGLY_PERCENT:
			return R.drawable.squiggly_p;
		case SQUIGGLY_COLOR:
			return R.drawable.squiggly_c;
		}
		throw new IllegalStateException();
	}
}

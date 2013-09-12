package com.whenfully.pandoku;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

import com.whenfully.pandoku.model.PuzzleType;

class ColorTheme implements Theme {
	private final String symbols;

	private final float borderStrokeWidth;

	private final Drawable background;
	private final int puzzleBackgroundColor;

	private final int nameTextColor;
	private final int difficultyTextColor;
	private final int sourceTextColor;
	private final int timerTextColor;

	private final Paint gridPaint;
	private final Paint regionBorderPaint;
	private final Paint extraRegionPaint;
	private final Paint[] colorSudokuExtraRegionPaints;
	private final Paint valuePaint;
	private final Paint digitPaint;
	private final Paint cluePaint;
	private final Paint previewCluePaint;
	private final Paint errorPaint;
	private final Paint markedPositionPaint;
	private final Paint markedPositionCluePaint;
	private final Paint outerBorderPaint;

	private final float outerBorderRadius;

	private final AreaColorPolicy areaColorPolicy;
	private final int[] areaColors2;
	private final int[] areaColors3;
	private final int[] areaColors4;

	private final HighlightDigitsPolicy highlightDigitsPolicy;
	private final int highlightedCellColorSingleDigit;
	private final int highlightedCellColorMultipleDigits;

	private final Drawable congratsDrawable;
	private final Drawable pausedDrawable;

	public static final class Builder {
		private final Resources resources;

		public String symbols = "123456789";
		public int[] backgroudColors = { 0xfff7f7f7 };
		public int puzzleBackgroundColor = 0xffffffff;
		public int nameTextColor = 0xff222222;
		public int difficultyTextColor = 0xff222222;
		public int sourceTextColor = 0xff222222;
		public int timerTextColor = 0xff222222;
		public int gridColor = 0x66000000;
		public int borderColor = 0xff000000;
		public int extraRegionColor = 0xcd99abff;
		public int[] colorSudokuExtraRegionColors = { 0x55ff0000, 0x55000000, 0x5500ff00, 0x55808080,
				0x5500ffff, 0x550000ff, 0x55ffff00, 0x55ff00ff, 0x55ffffff };
		public int valueColor = 0xff003000;
		public int clueColor = 0xff000000;
		public int errorColor = 0xffff0000;
		public int markedPositionColor = 0xff00ff00;
		public int markedPositionClueColor = 0xb0ff0000;
		public AreaColorPolicy areaColorPolicy = AreaColorPolicy.STANDARD_X_HYPER_SQUIGGLY;
		public int[] areaColors2 = { 0xffffffff, 0xffe0e0e0 };
		public int[] areaColors3 = Util.colorRing(0xffffd9d9, 3);
		public int[] areaColors4 = Util.colorRing(0xffffffd9, 4);
		public HighlightDigitsPolicy highlightDigitsPolicy = HighlightDigitsPolicy.ONLY_SINGLE_VALUES;
		public int highlightedCellColorSingleDigit = 0xe6ffff00;
		public int highlightedCellColorMultipleDigits = 0xe6bebe00;

		public Builder(Resources resources) {
			this.resources = resources;
		}

		public Theme build() {
			return new ColorTheme(this);
		}
	}

	private ColorTheme(Builder builder) {
		Resources resources = builder.resources;

		float displayDensity = resources.getDisplayMetrics().density;
		float gridWidth = Math.max(1, Math.round(displayDensity));

		symbols = builder.symbols;

		borderStrokeWidth = Math.max(2, Math.round(3 * displayDensity));

		background = createBackgroundDrawable(builder);

		puzzleBackgroundColor = builder.puzzleBackgroundColor;

		nameTextColor = builder.nameTextColor;
		difficultyTextColor = builder.difficultyTextColor;
		sourceTextColor = builder.sourceTextColor;
		timerTextColor = builder.timerTextColor;

		gridPaint = new Paint();
		gridPaint.setStrokeWidth(gridWidth);
		gridPaint.setAntiAlias(false);
		gridPaint.setColor(builder.gridColor);
		gridPaint.setStrokeCap(Cap.BUTT);
		// gridPaint.setShadowLayer(1, 1, 1, 0xff000000);

		regionBorderPaint = new Paint();
		regionBorderPaint.setStrokeWidth(borderStrokeWidth);
		regionBorderPaint.setAntiAlias(false);
		regionBorderPaint.setColor(builder.borderColor);
		regionBorderPaint.setStrokeCap(Cap.ROUND);

		extraRegionPaint = new Paint();
		extraRegionPaint.setAntiAlias(false);
		extraRegionPaint.setColor(builder.extraRegionColor);

		colorSudokuExtraRegionPaints = new Paint[builder.colorSudokuExtraRegionColors.length];
		for (int i = 0; i < builder.colorSudokuExtraRegionColors.length; i++) {
			colorSudokuExtraRegionPaints[i] = new Paint();
			colorSudokuExtraRegionPaints[i].setAntiAlias(false);
			colorSudokuExtraRegionPaints[i].setColor(builder.colorSudokuExtraRegionColors[i]);
		}

		Typeface typeface = Typeface.SANS_SERIF;
		valuePaint = new Paint();
		valuePaint.setAntiAlias(true);
		valuePaint.setColor(builder.valueColor);
		valuePaint.setTextAlign(Align.CENTER);
		valuePaint.setTypeface(typeface);

		Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
		digitPaint = new Paint();
		digitPaint.setAntiAlias(true);
		digitPaint.setColor(builder.valueColor);
		digitPaint.setTextAlign(Align.CENTER);
		digitPaint.setTypeface(boldTypeface);

		cluePaint = new Paint();
		cluePaint.setAntiAlias(true);
		cluePaint.setColor(builder.clueColor);
		cluePaint.setTextAlign(Align.CENTER);
		cluePaint.setTypeface(boldTypeface);

		previewCluePaint = new Paint(cluePaint);
		previewCluePaint.setAlpha(128);

		errorPaint = new Paint();
		errorPaint.setStrokeWidth(borderStrokeWidth);
		errorPaint.setAntiAlias(true);
		errorPaint.setColor(builder.errorColor);
		errorPaint.setStyle(Style.STROKE);
		errorPaint.setStrokeCap(Cap.BUTT);

		markedPositionPaint = new Paint();
		markedPositionPaint.setAntiAlias(false);
		markedPositionPaint.setColor(builder.markedPositionColor);

		markedPositionCluePaint = new Paint();
		markedPositionCluePaint.setAntiAlias(false);
		markedPositionCluePaint.setColor(builder.markedPositionClueColor);

		outerBorderPaint = new Paint();
		outerBorderPaint.setStrokeWidth(Math.round(borderStrokeWidth));
		outerBorderPaint.setAntiAlias(true);
		outerBorderPaint.setColor(builder.borderColor);
		outerBorderPaint.setStyle(Style.STROKE);

		outerBorderRadius = 6 * displayDensity;

		areaColorPolicy = builder.areaColorPolicy;
		areaColors2 = copy(builder.areaColors2, 2);
		areaColors3 = copy(builder.areaColors3, 3);
		areaColors4 = copy(builder.areaColors4, 4);

		highlightDigitsPolicy = builder.highlightDigitsPolicy;
		highlightedCellColorSingleDigit = builder.highlightedCellColorSingleDigit;
		highlightedCellColorMultipleDigits = builder.highlightedCellColorMultipleDigits;

		congratsDrawable = resources.getDrawable(R.drawable.congrats);
		congratsDrawable.setAlpha(144);

		pausedDrawable = resources.getDrawable(R.drawable.paused);
		pausedDrawable.setAlpha(144);
	}

	private Drawable createBackgroundDrawable(Builder builder) {
		switch (builder.backgroudColors.length) {
			case 0:
				throw new IllegalArgumentException();
			case 1:
				return new ColorDrawable(builder.backgroudColors[0]);
			default:
				return new GradientDrawable(Orientation.TOP_BOTTOM, builder.backgroudColors);
		}
	}

	private int[] copy(int[] colors, int length) {
		if (colors.length != length)
			throw new IllegalArgumentException();

		int[] copy = new int[length];
		System.arraycopy(colors, 0, copy, 0, length);
		return copy;
	}

	public char getSymbol(int value) {
		return symbols.charAt(value);
	}

	public int[] getPuzzlePadding() {
		final int padding = Math.round(borderStrokeWidth);
		return new int[] { padding, padding, padding, padding };
	}

	public Drawable getBackground() {
		return background;
	}

	public int getPuzzleBackgroundColor() {
		return puzzleBackgroundColor;
	}

	public int getNameTextColor() {
		return nameTextColor;
	}

	public int getLevelTextColor() {
		return difficultyTextColor;
	}

	public int getSourceTextColor() {
		return sourceTextColor;
	}

	public int getTimerTextColor() {
		return timerTextColor;
	}

	public Paint getGridPaint() {
		return gridPaint;
	}

	public Paint getRegionBorderPaint() {
		return regionBorderPaint;
	}

	public Paint getExtraRegionPaint(PuzzleType puzzleType, int extraRegionCode) {
		if (puzzleType == PuzzleType.STANDARD_COLOR || puzzleType == PuzzleType.SQUIGGLY_COLOR)
			return colorSudokuExtraRegionPaints[extraRegionCode % colorSudokuExtraRegionPaints.length];
		else
			return extraRegionPaint;
	}

	public Paint getValuePaint() {
		return valuePaint;
	}

	public Paint getDigitPaint() {
		return digitPaint;
	}

	public Paint getCluePaint(boolean preview) {
		return preview ? previewCluePaint : cluePaint;
	}

	public Paint getErrorPaint() {
		return errorPaint;
	}

	public Paint getMarkedPositionPaint() {
		return markedPositionPaint;
	}

	public Paint getMarkedPositionCluePaint() {
		return markedPositionCluePaint;
	}

	public Paint getOuterBorderPaint() {
		return outerBorderPaint;
	}

	public float getOuterBorderRadius() {
		return outerBorderRadius;
	}

	public boolean isDrawAreaColors(PuzzleType puzzleType) {
		return areaColorPolicy.matches(puzzleType);
	}

	public int getAreaColor(int colorNumber, int numberOfColors) {
		switch (numberOfColors) {
			case 2:
				return areaColors2[colorNumber];
			case 3:
				return areaColors3[colorNumber];
			case 4:
				return areaColors4[colorNumber];
			default:
				return areaColors4[colorNumber % 4];
		}
	}

	public HighlightDigitsPolicy getHighlightDigitsPolicy() {
		return highlightDigitsPolicy;
	}

	public int getHighlightedCellColorSingleDigit() {
		return highlightedCellColorSingleDigit;
	}

	public int getHighlightedCellColorMultipleDigits() {
		return highlightedCellColorMultipleDigits;
	}

	public Drawable getCongratsDrawable() {
		return congratsDrawable;
	}

	public Drawable getPausedDrawable() {
		return pausedDrawable;
	}
}

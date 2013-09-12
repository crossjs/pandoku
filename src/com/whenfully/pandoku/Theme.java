package com.whenfully.pandoku;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.whenfully.pandoku.model.PuzzleType;

interface Theme {
	char getSymbol(int value);

	int[] getPuzzlePadding();

	Drawable getBackground();

	int getPuzzleBackgroundColor();

	int getNameTextColor();
	int getLevelTextColor();
	int getSourceTextColor();
	int getTimerTextColor();

	Paint getGridPaint();
	Paint getRegionBorderPaint();
	Paint getExtraRegionPaint(PuzzleType puzzleType, int extraRegionCode);
	Paint getValuePaint();
	Paint getDigitPaint();
	Paint getCluePaint(boolean preview);
	Paint getErrorPaint();
	Paint getMarkedPositionPaint();
	Paint getMarkedPositionCluePaint();
	Paint getOuterBorderPaint();

	float getOuterBorderRadius();

	boolean isDrawAreaColors(PuzzleType puzzleType);
	int getAreaColor(int colorNumber, int numberOfColors);

	HighlightDigitsPolicy getHighlightDigitsPolicy();
	int getHighlightedCellColorSingleDigit();
	int getHighlightedCellColorMultipleDigits();

	Drawable getCongratsDrawable();
	Drawable getPausedDrawable();
}

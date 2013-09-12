package com.whenfully.pandoku;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

import com.whenfully.pandoku.model.ValueSet;

public class MultiValuesPainter {
	private Theme theme;

	private float textOffset;
	private float baselineDist;
	private float xOffset;

	private float cellWidth;
	private float cellHeight;

	public MultiValuesPainter() {
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public void setPuzzleSize(int puzzleSize) {
		// TODO
	}

	public void setCellSize(float cellSizeX, float cellSizeY) {
		this.cellWidth = cellSizeX;
		this.cellHeight = cellSizeY;

		float fontSize = cellHeight * 0.3f;
		setFontSize(fontSize);
	}

	public void paintValues(Canvas canvas, ValueSet values) {
		for (int value = values.nextValue(0); value != -1; value = values.nextValue(value + 1)) {
			int vrow = value / 3;
			int vcol = value % 3;
			String dv = String.valueOf(theme.getSymbol(value));
			float py = textOffset + vrow * baselineDist;
			float px = vcol == 0 ? xOffset : (vcol == 1 ? cellWidth / 2f : cellWidth - xOffset);
			Paint paint = theme.getDigitPaint();
			canvas.drawText(dv, px, py, paint);
		}
	}

	private void setFontSize(float fontSize) {
		Paint digitPaint = theme.getDigitPaint();
		digitPaint.setTextSize(fontSize);

		FontMetrics fontMetrics = digitPaint.getFontMetrics();
		float fontHeight = -fontMetrics.ascent - fontMetrics.descent;
		int rows = 3;
		float spacing = (cellHeight - rows * fontHeight) / (rows + 1);
		baselineDist = fontHeight + spacing;
		textOffset = cellHeight - spacing - (rows - 1) * baselineDist + 0.5f;
		xOffset = spacing + digitPaint.measureText("5") / 2;
	}
}

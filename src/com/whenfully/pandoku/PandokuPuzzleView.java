package com.whenfully.pandoku;

import java.util.HashSet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.whenfully.pandoku.model.PandokuPuzzle;
import com.whenfully.pandoku.model.Level;
import com.whenfully.pandoku.model.Position;
import com.whenfully.pandoku.model.Puzzle;
import com.whenfully.pandoku.model.RegionError;
import com.whenfully.pandoku.model.ValueSet;
import com.whenfully.pandoku.transfer.PuzzleDecoder;

public class PandokuPuzzleView extends View {
	private static final String TAG = PandokuPuzzleView.class.getName();

	private static final int PREF_SIZE = 300;

	private PandokuPuzzle puzzle;
	private int size;
	private boolean paused;
	private boolean preview;

	private Theme theme;

	private float textOffset;

	private final MultiValuesPainter multiValuesPainter = new MultiValuesPainter();

	private float cellWidth;
	private float cellHeight;
	private float offsetX;
	private float offsetY;

	private float textSize = 1;

	private int previewClueCounter;

	private Integer highlightedDigit;

	private Position markedPosition;

	public PandokuPuzzleView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setFocusable(true); // make sure we get key events

		if (isInEditMode()) {
			setTheme(new ColorTheme.Builder(getResources()).build());
			Puzzle puzzle = PuzzleDecoder
					.decode("..7....638.467.9...1..39..2..37..6..7..4.1..5..8..61..6..21..9...1.635.839....7..");
			setPuzzle(new PandokuPuzzle("Preview Puzzle", puzzle, Level.EASY));
		}
	}

	public void setTheme(Theme theme) {
		this.theme = theme;

		setCurrentTextSizeOnTheme();

		multiValuesPainter.setTheme(theme);

		int[] padding = theme.getPuzzlePadding();
		setPadding(padding[0], padding[1], padding[2], padding[3]);
	}

	public void setPuzzle(PandokuPuzzle puzzle) {
		this.puzzle = puzzle;
		size = puzzle == null ? 0 : puzzle.getSize();
		multiValuesPainter.setPuzzleSize(size);

		requestLayout();
		invalidate();
	}

	public PandokuPuzzle getPuzzle() {
		return puzzle;
	}

	public Position getPositionAt(float px, float py, float frameScale, float maxFrame) {
		if (puzzle == null)
			return null;

		px -= offsetX;
		final float frameX = Math.min(maxFrame, cellWidth * frameScale);
		if (px < -frameX || px >= cellWidth * size + frameX)
			return null;

		py -= offsetY;
		final float frameY = Math.min(maxFrame, cellHeight * frameScale);
		if (py < -frameY || py >= cellHeight * size + frameY)
			return null;

		int cx = Math.max(0, Math.min(size - 1, (int) Math.floor(px / cellWidth)));
		int cy = Math.max(0, Math.min(size - 1, (int) Math.floor(py / cellHeight)));

		return new Position(cy, cx);
	}

	public PointF getPositionCenterPoint(Position position) {
		float x = position.col * cellWidth + cellWidth / 2 + offsetX;
		float y = position.row * cellHeight + cellHeight / 2 + offsetY;
		return new PointF(x, y);
	}

	public void highlightDigit(Integer digit) {
		if (highlightedDigit == digit)
			return;

		highlightedDigit = digit;

		invalidate();
	}

	public Integer getHighlightedDigit() {
		return highlightedDigit;
	}

	public void markPosition(Position position) {
		if (eq(position, markedPosition))
			return;

		invalidatePosition(position);
		invalidatePosition(markedPosition);

		markedPosition = position;
	}

	public Position getMarkedPosition() {
		return markedPosition;
	}

	public void invalidatePosition(Position position) {
		if (position == null || puzzle == null)
			return;

		if (Constants.LOG_V)
			Log.v(TAG, "invalidatePosition(" + position + ")");

		float x0 = offsetX + position.col * cellWidth;
		float x1 = x0 + cellWidth;
		float y0 = offsetY + position.row * cellHeight;
		float y1 = y0 + cellHeight;
		invalidate((int) Math.floor(x0), (int) Math.floor(y0), (int) Math.ceil(x1), (int) Math
				.ceil(y1));
	}

	public void setPaused(boolean paused) {
		if (this.paused == paused)
			return;

		this.paused = paused;
		invalidate();
	}

	public void setPreview(boolean preview) {
		if (this.preview == preview)
			return;

		this.preview = preview;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (puzzle == null)
			return;

		long t0 = System.currentTimeMillis();
		onDraw0(canvas);
		long t1 = System.currentTimeMillis();

		if (Constants.LOG_V)
			Log.v(TAG, "Draw time: " + (t1 - t0));
	}

	private void onDraw0(Canvas canvas) {
		if (Constants.LOG_V)
			Log.v(TAG, "onDraw(" + canvas.getClipBounds() + ")");

		canvas.save();
		canvas.translate(offsetX, offsetY);
		canvas.clipRect(0, 0, size * cellWidth, size * cellHeight);

		Rect clipBounds = canvas.getClipBounds();

		if (theme.isDrawAreaColors(puzzle.getPuzzleType()))
			drawAreaColors(canvas, clipBounds);
		else
			drawBackground(canvas);

		drawExtraRegions(canvas, clipBounds);

		if (puzzle.isSolved())
			drawCongrats(canvas);
		else if (paused)
			drawPaused(canvas);
		else {
			drawHighlightedCells(canvas, clipBounds);

			drawMarkedPosition(canvas);
		}

		if (!preview && puzzle.hasErrors())
			drawErrors(canvas, clipBounds);

		drawValues(canvas, clipBounds);

		drawGrid(canvas);

		drawRegionBorders(canvas, clipBounds);

		canvas.restore();

		drawOuterBorder(canvas);
	}

	private void drawAreaColors(Canvas canvas, Rect clipBounds) {
		for (int row = 0; row < size; row++) {
			float y = row * cellHeight;
			if (y > clipBounds.bottom || y + cellHeight < clipBounds.top)
				continue;

			for (int col = 0; col < size; col++) {
				float x = col * cellWidth;
				if (x > clipBounds.right || x + cellWidth < clipBounds.left)
					continue;

				canvas.save();
				canvas.translate(x, y);

				drawAreaColors(canvas, row, col);

				canvas.restore();
			}
		}
	}

	private void drawAreaColors(Canvas canvas, int row, int col) {
		int colorNumber = puzzle.getAreaColor(row, col);
		int color = theme.getAreaColor(colorNumber, puzzle.getNumberOfAreaColors());

		canvas.clipRect(0, 0, cellWidth, cellHeight);
		canvas.drawColor(color);
	}

	private void drawBackground(Canvas canvas) {
		canvas.drawColor(theme.getPuzzleBackgroundColor());
	}

	private void drawExtraRegions(Canvas canvas, Rect clipBounds) {
		for (int row = 0; row < size; row++) {
			float y = row * cellHeight;
			if (y > clipBounds.bottom || y + cellHeight < clipBounds.top)
				continue;

			for (int col = 0; col < size; col++) {
				float x = col * cellWidth;
				if (x > clipBounds.right || x + cellWidth < clipBounds.left)
					continue;

				canvas.save();
				canvas.translate(x, y);

				drawExtraRegions(canvas, row, col);

				canvas.restore();
			}
		}
	}

	private void drawExtraRegions(Canvas canvas, int row, int col) {
		if (puzzle.isExtraRegion(row, col)) {
			canvas.drawRect(0, 0, cellWidth, cellHeight, theme.getExtraRegionPaint(puzzle
					.getPuzzleType(), puzzle.getExtraRegionCode(row, col)));
		}
	}

	private void drawCongrats(Canvas canvas) {
		Drawable congratsDrawable = theme.getCongratsDrawable();
		congratsDrawable.setBounds(0, 0, Math.round(size * cellWidth), Math.round(size * cellHeight));
		congratsDrawable.draw(canvas);
	}

	private void drawPaused(Canvas canvas) {
		Drawable pausedDrawable = theme.getPausedDrawable();
		pausedDrawable.setBounds(0, 0, Math.round(size * cellWidth), Math.round(size * cellHeight));
		pausedDrawable.draw(canvas);
	}

	private void drawHighlightedCells(Canvas canvas, Rect clipBounds) {
		if (highlightedDigit == null
				|| theme.getHighlightDigitsPolicy() == HighlightDigitsPolicy.NEVER)
			return;

		for (int row = 0; row < size; row++) {
			float y = row * cellHeight;
			if (y > clipBounds.bottom || y + cellHeight < clipBounds.top)
				continue;

			for (int col = 0; col < size; col++) {
				float x = col * cellWidth;
				if (x > clipBounds.right || x + cellWidth < clipBounds.left)
					continue;

				final ValueSet values = puzzle.getValues(row, col);
				if (values.contains(highlightedDigit)) {
					drawHighlightedcell(canvas, values.size(), x, y);
				}
			}
		}
	}

	private void drawHighlightedcell(Canvas canvas, int numValues, float x, float y) {
		if (numValues != 1
				&& theme.getHighlightDigitsPolicy() == HighlightDigitsPolicy.ONLY_SINGLE_VALUES)
			return;

		canvas.save();
		canvas.translate(x, y);

		canvas.clipRect(0, 0, cellWidth, cellHeight);

		if (numValues == 1)
			canvas.drawColor(theme.getHighlightedCellColorSingleDigit());
		else
			canvas.drawColor(theme.getHighlightedCellColorMultipleDigits());

		canvas.restore();
	}

	private void drawMarkedPosition(Canvas canvas) {
		if (markedPosition == null)
			return;

		canvas.save();

		float x = markedPosition.col * cellWidth;
		float y = markedPosition.row * cellHeight;
		canvas.translate(x, y);

		Paint paint = puzzle.isClue(markedPosition.row, markedPosition.col) ? theme
				.getMarkedPositionCluePaint() : theme.getMarkedPositionPaint();
		canvas.clipRect(0, 0, cellWidth, cellHeight);
		canvas.drawPaint(paint);

		canvas.restore();
	}

	private void drawErrors(Canvas canvas, Rect clipBounds) {
		Paint errorPaint = theme.getErrorPaint();

		float radius = Math.min(cellWidth, cellHeight) * 0.4f;

		final HashSet<RegionError> regionErrors = puzzle.getRegionErrors();

		final HashSet<Position> regionErrorPositions = getUniquePositions(regionErrors);
		for (Position p : regionErrorPositions) {
			float x = p.col * cellWidth;
			if (x > clipBounds.right || x + cellWidth < clipBounds.left)
				continue;
			float y = p.row * cellHeight;
			if (y > clipBounds.bottom || y + cellHeight < clipBounds.top)
				continue;

			float cx = x + cellWidth / 2;
			float cy = y + cellHeight / 2;
			canvas.drawCircle(cx, cy, radius, errorPaint);
		}

		for (RegionError error : regionErrors) {
			float cx1 = error.p1.col * cellWidth + cellWidth / 2;
			float cy1 = error.p1.row * cellHeight + cellHeight / 2;

			float cx2 = error.p2.col * cellWidth + cellWidth / 2;
			float cy2 = error.p2.row * cellHeight + cellHeight / 2;

			if (cx1 == cx2) // vertical line
			{
				float vy = cy2 - cy1;
				vy *= (radius / Math.abs(vy));

				canvas.drawLine(cx1, cy1 + vy, cx2, cy2 - vy, errorPaint);
			}
			else if (cy1 == cy2) // horizontal line
			{
				float vx = cx2 - cx1;
				vx *= (radius / Math.abs(vx));

				canvas.drawLine(cx1 + vx, cy1, cx2 - vx, cy2, errorPaint);
			}
			else {
				float vx = cx2 - cx1;
				float vy = cy2 - cy1;
				float scale = (float) (radius / Math.sqrt(vx * vx + vy * vy));
				vx *= scale;
				vy *= scale;

				canvas.drawLine(cx1 + vx, cy1 + vy, cx2 - vx, cy2 - vy, errorPaint);
			}
		}

		for (Position p : puzzle.getCellErrors()) {
			float x = p.col * cellWidth;
			if (x > clipBounds.right || x + cellWidth < clipBounds.left)
				continue;
			float y = p.row * cellHeight;
			if (y > clipBounds.bottom || y + cellHeight < clipBounds.top)
				continue;

			float cx = x + cellWidth / 2;
			float cy = y + cellHeight / 2;
			float delta = radius / 1.41421356f;
			canvas.drawLine(cx - delta, cy - delta, cx + delta, cy + delta, errorPaint);
			canvas.drawLine(cx + delta, cy - delta, cx - delta, cy + delta, errorPaint);
		}
	}

	private HashSet<Position> getUniquePositions(final HashSet<RegionError> regionErrors) {
		HashSet<Position> positions = new HashSet<Position>();

		for (RegionError error : regionErrors) {
			positions.add(error.p1);
			positions.add(error.p2);
		}

		return positions;
	}

	private void drawValues(Canvas canvas, Rect clipBounds) {
		previewClueCounter = 0;

		for (int row = 0; row < size; row++) {
			float y = row * cellHeight;
			if (y > clipBounds.bottom || y + cellHeight < clipBounds.top)
				continue;

			for (int col = 0; col < size; col++) {
				float x = col * cellWidth;
				if (x > clipBounds.right || x + cellWidth < clipBounds.left)
					continue;

				canvas.save();
				canvas.translate(x, y);

				drawValues(canvas, row, col);

				canvas.restore();
			}
		}
	}

	private void drawValues(Canvas canvas, int row, int col) {
		ValueSet values = puzzle.getValues(row, col);
		if (values.isEmpty())
			return;

		if (preview && !puzzle.isSolved()) {
			if (puzzle.isClue(row, col)) {
				boolean show = previewClueCounter++ % 3 != 0;
				String dv = show ? String.valueOf(theme.getSymbol(values.nextValue(0))) : "?";
				canvas.drawText(dv, cellWidth / 2f, textOffset, theme.getCluePaint(preview));
			}
		}
		else if (values.size() == 1) {
			String dv = String.valueOf(theme.getSymbol(values.nextValue(0)));
			Paint paint = puzzle.isClue(row, col) ? theme.getCluePaint(preview) : theme
					.getValuePaint();
			canvas.drawText(dv, cellWidth / 2f, textOffset, paint);
		}
		else {
			multiValuesPainter.paintValues(canvas, values);
		}
	}

	private void drawGrid(Canvas canvas) {
		Paint gridPaint = theme.getGridPaint();

		float gridWidth = size * cellWidth;
		float gridHeight = size * cellHeight;
		for (int i = 1; i < size; i++) {
			float x = i * cellWidth;
			float y = i * cellHeight;
			canvas.drawLine(0, y, gridWidth, y, gridPaint);
			canvas.drawLine(x, 0, x, gridHeight, gridPaint);
		}
	}

	private void drawRegionBorders(Canvas canvas, Rect clipBounds) {
		Paint regionBorderPaint = theme.getRegionBorderPaint();

		for (int row = 0; row < size; row++) {
			float y = row * cellHeight;
			if (y > clipBounds.bottom || y + cellHeight < clipBounds.top)
				continue;

			for (int col = 0; col < size; col++) {
				float x = col * cellWidth;
				if (x > clipBounds.right || x + cellWidth < clipBounds.left)
					continue;

				if (row > 0 && puzzle.getAreaCode(row, col) != puzzle.getAreaCode(row - 1, col)) {
					canvas.drawLine(x, y, x + cellWidth, y, regionBorderPaint);
				}
				if (col > 0 && puzzle.getAreaCode(row, col) != puzzle.getAreaCode(row, col - 1)) {
					canvas.drawLine(x, y, x, y + cellHeight, regionBorderPaint);
				}
			}
		}
	}

	private void drawOuterBorder(Canvas canvas) {
		Paint paint = theme.getOuterBorderPaint();
		float radius = theme.getOuterBorderRadius();
		float width = paint.getStrokeWidth() / 2;
		RectF rect = new RectF(width, width, getMeasuredWidth() - width, getMeasuredHeight() - width);
		canvas.drawRoundRect(rect, radius, radius, paint);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (Constants.LOG_V)
			Log.v(TAG, "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
					+ MeasureSpec.toString(heightMeasureSpec) + ")");

		int wMode = MeasureSpec.getMode(widthMeasureSpec);
		int wSize = MeasureSpec.getSize(widthMeasureSpec);
		int hMode = MeasureSpec.getMode(heightMeasureSpec);
		int hSize = MeasureSpec.getSize(heightMeasureSpec);

		if (wMode == MeasureSpec.EXACTLY) {
			if (hMode == MeasureSpec.EXACTLY) {
				setSize(wSize, hSize);
			}
			else if (hMode == MeasureSpec.AT_MOST) {
				setSize(wSize, Math.min(wSize, hSize));
			}
			else {
				setSize(wSize, wSize);
			}
		}
		else if (wMode == MeasureSpec.AT_MOST) {
			if (hMode == MeasureSpec.EXACTLY) {
				// f*ck exact height for vertical LinearLayout to work as desired
				// setSize(Math.min(wSize, hSize), hSize);
				setSize(Math.min(wSize, hSize), Math.min(wSize, hSize));
			}
			else if (hMode == MeasureSpec.AT_MOST) {
				setSize(Math.min(wSize, hSize), Math.min(wSize, hSize));
			}
			else {
				setSize(wSize, wSize);
			}
		}
		else {
			if (hMode == MeasureSpec.EXACTLY) {
				setSize(hSize, hSize);
			}
			else if (hMode == MeasureSpec.AT_MOST) {
				setSize(hSize, hSize);
			}
			else {
				setSize(PREF_SIZE, PREF_SIZE);
			}
		}
	}

	private void setSize(int width, int height) {
		if (Constants.LOG_V)
			Log.v(TAG, "setSize(" + width + ", " + height + ")");

		setMeasuredDimension(width, height);

		int gridWidth = width - getPaddingLeft() - getPaddingRight();
		int gridHeight = height - getPaddingTop() - getPaddingBottom();
		cellWidth = gridWidth / (float) size;
		cellHeight = gridHeight / (float) size;
		offsetX = getPaddingLeft();
		offsetY = getPaddingTop();

		textSize = Math.min(cellWidth, cellHeight) * 0.8f;
		setCurrentTextSizeOnTheme();
		calcTextOffset();

		multiValuesPainter.setCellSize(cellWidth, cellHeight);
	}

	private void setCurrentTextSizeOnTheme() {
		if (theme != null) {
			theme.getValuePaint().setTextSize(textSize);
			theme.getCluePaint(true).setTextSize(textSize);
			theme.getCluePaint(false).setTextSize(textSize);
		}
	}

	private void calcTextOffset() {
		FontMetrics fontMetrics = theme.getValuePaint().getFontMetrics();
		float fontSize = -fontMetrics.ascent - fontMetrics.descent;
		textOffset = cellHeight - (cellHeight - fontSize) / 2 + 0.5f;
	}

	private boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
}

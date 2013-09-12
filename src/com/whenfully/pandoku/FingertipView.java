package com.whenfully.pandoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;

public class FingertipView extends View {
	private PointF point;
	private boolean editable;

	private final int radiusX;
	private final int radiusY;

	private Drawable redFingertip;
	private Drawable greenFingertip;

	public FingertipView(Context context) {
		super(context);

		float displayDensity = getResources().getDisplayMetrics().density;
		int radius = Math.round(75 * displayDensity);

		redFingertip = createRadialGradient(0xff0000, radius);
		greenFingertip = createRadialGradient(0x00aa00, radius);

		radiusX = radius;
		radiusY = radius;
	}

	private static GradientDrawable createRadialGradient(int color, int radius) {
		int[] colors = new int[] { 0xff000000 | color, 0x00ffffff & color };
		GradientDrawable gradient = new GradientDrawable(Orientation.TL_BR, colors);
		gradient.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		gradient.setGradientRadius(radius);
		gradient.setGradientCenter(0.5f, 0.5f);
		return gradient;
	}

	public void hide() {
		show(null, false);
	}

	public void show(PointF point, boolean editable) {
		if (eq(this.point, point) && this.editable == editable)
			return;

		invalidatePoint(this.point);
		invalidatePoint(point);

		this.point = point;
		this.editable = editable;
	}

	private void invalidatePoint(PointF point) {
		if (point == null)
			return;

		invalidate((int) Math.floor(point.x - radiusX), (int) Math.floor(point.y - radiusY),
				(int) Math.ceil(point.x + radiusX), (int) Math.ceil(point.y + radiusY));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (point == null)
			return;

		Drawable drawable = editable ? greenFingertip : redFingertip;

		final int cx = Math.round(point.x);
		final int cy = Math.round(point.y);
		drawable.setBounds(cx - radiusX, cy - radiusY, cx + radiusX - 1, cy + radiusY - 1);
		drawable.draw(canvas);
	}

	private boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
}

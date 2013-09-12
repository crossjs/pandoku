package com.whenfully.pandoku;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class KeypadButton extends ImageButton {
	private static final int[] HIGHLIGHTED_STATE_SET = { R.attr.state_highlighted };

	private boolean highlighted;

	public KeypadButton(Context context) {
		this(context, null);
	}

	public KeypadButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.buttonStyleKeypad);
	}

	public KeypadButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeypadButton, defStyle, 0);
		highlighted = a.getBoolean(R.styleable.KeypadButton_highlighted, false);

		a.recycle();
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) {
			this.highlighted = highlighted;
			refreshDrawableState();
		}
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (highlighted)
			mergeDrawableStates(drawableState, HIGHLIGHTED_STATE_SET);
		return drawableState;
	}
}

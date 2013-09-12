package com.whenfully.pandoku;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class KeypadToggleButton extends KeypadButton {
	private static final int[] CHECKED_STATE_SET = { R.attr.state_checked };

	private boolean checked;

	public KeypadToggleButton(Context context) {
		this(context, null);
	}

	public KeypadToggleButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.buttonStyleKeypadToggle);
	}

	public KeypadToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeypadToggleButton,
				defStyle, 0);
		checked = a.getBoolean(R.styleable.KeypadToggleButton_checked, false);

		a.recycle();
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		if (this.checked != checked) {
			this.checked = checked;
			refreshDrawableState();
		}
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (checked)
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		return drawableState;
	}
}

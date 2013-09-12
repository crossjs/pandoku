package com.whenfully.pandoku.im;

import android.os.Bundle;

import com.whenfully.pandoku.model.Position;
import com.whenfully.pandoku.model.ValueSet;

public class ValuesThenCellInputMethod implements InputMethod {
	private static final String APP_STATE_KEYPAD_VALUES = "keypadValues";

	private final InputMethodTarget target;

	private final ValueSet values = new ValueSet();

	public ValuesThenCellInputMethod(InputMethodTarget target) {
		this.target = target;
	}

	public boolean isValuesEmpty() {
		return values.isEmpty();
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(APP_STATE_KEYPAD_VALUES, values.toInt());
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		int v = savedInstanceState.getInt(APP_STATE_KEYPAD_VALUES, 0);
		setValues(v);
	}

	public void reset() {
		target.setMarkedPosition(null);
		target.highlightDigit(null);
		setValues(0);
	}

	public void onMoveMark(int dy, int dx) {
	}

	public void onKeypad(int digit) {
		if (values.contains(digit)) {
			values.remove(digit);
			target.checkButton(digit, false);
		}
		else {
			values.add(digit);
			target.checkButton(digit, true);
		}

		if (values.isEmpty())
			target.highlightDigit(null);
		else
			target.highlightDigit(digit);
	}

	public void onClear() {
		setValues(0);

		target.highlightDigit(null);
	}

	public void onInvert() {
		final int nButtons = target.getNumberOfDigitButtons();
		for (int digit = 0; digit < nButtons; digit++) {
			if (values.contains(digit))
				values.remove(digit);
			else
				values.add(digit);
		}

		checkButtons();
	}

	public void onSweep() {
	}

	public void onTap(Position position, boolean editable) {
		if (!editable || values.isEmpty())
			return;

		ValueSet cellValues = target.getCellValues(position);
		if (cellValues.containsAny(values)) {
			cellValues.removeAll(values);
			target.setCellValues(position, cellValues);
		}
		else {
			cellValues.addAll(values);
			target.setCellValues(position, cellValues);
		}
	}

	public void onValuesChanged() {
	}

	private void setValues(int v) {
		values.setFromInt(v);

		checkButtons();
	}

	private void checkButtons() {
		final int nButtons = target.getNumberOfDigitButtons();
		for (int digit = 0; digit < nButtons; digit++)
			target.checkButton(digit, values.contains(digit));
	}
}

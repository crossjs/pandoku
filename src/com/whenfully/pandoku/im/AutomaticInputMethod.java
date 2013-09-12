package com.whenfully.pandoku.im;

import android.os.Bundle;

import com.whenfully.pandoku.model.Position;

public class AutomaticInputMethod implements InputMethod {
	private static final String APP_STATE_ACTIVE_INPUT_METHOD = "automaticInputMethod";
	private static final String IM_UNDECIDED = "undecided";
	private static final String IM_CELL_THEN_VALUES = "cellThenValues";
	private static final String IM_VALUES_THEN_CELL = "valuesThenCell";

	private final InputMethodTarget target;
	private final CellThenValuesInputMethod cellThenValues;
	private final ValuesThenCellInputMethod valuesThenCell;
	private InputMethod activeInputMethod = null;
	private Position lastMarkedPosition;

	public AutomaticInputMethod(InputMethodTarget target) {
		this.target = target;
		cellThenValues = new CellThenValuesInputMethod(target);
		valuesThenCell = new ValuesThenCellInputMethod(target);
	}

	public void onSaveInstanceState(Bundle outState) {
		if (activeInputMethod == null) {
			outState.putString(APP_STATE_ACTIVE_INPUT_METHOD, IM_UNDECIDED);
		}
		else if (activeInputMethod == cellThenValues) {
			outState.putString(APP_STATE_ACTIVE_INPUT_METHOD, IM_CELL_THEN_VALUES);
			cellThenValues.onSaveInstanceState(outState);
		}
		else {
			outState.putString(APP_STATE_ACTIVE_INPUT_METHOD, IM_VALUES_THEN_CELL);
			valuesThenCell.onSaveInstanceState(outState);
		}
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		String inputMethod = savedInstanceState.getString(APP_STATE_ACTIVE_INPUT_METHOD);
		if (inputMethod == null || inputMethod.equals(IM_UNDECIDED)) {
			setUndecided();
		}
		else if (inputMethod.equals(IM_CELL_THEN_VALUES)) {
			activeInputMethod = cellThenValues;
			cellThenValues.onRestoreInstanceState(savedInstanceState);
		}
		else {
			activeInputMethod = valuesThenCell;
			valuesThenCell.onRestoreInstanceState(savedInstanceState);
		}
	}

	public void reset() {
		cellThenValues.reset();
		valuesThenCell.reset();
		setUndecided();
	}

	public void onMoveMark(int dy, int dx) {
		ifUndecidedUseCellThenValues();

		activeInputMethod.onMoveMark(dy, dx);
	}

	public void onKeypad(int digit) {
		ifUndecidedUseValuesThenCells();

		activeInputMethod.onKeypad(digit);

		if (activeInputMethod == valuesThenCell && valuesThenCell.isValuesEmpty()) {
			setUndecided();
		}
	}

	public void onClear() {
		if (activeInputMethod != null)
			activeInputMethod.onClear();

		if (activeInputMethod == valuesThenCell)
			setUndecided();
	}

	public void onInvert() {
		if (target.getMarkedPosition() == null)
			ifUndecidedUseValuesThenCells();
		else
			ifUndecidedUseCellThenValues();

		activeInputMethod.onInvert();

		if (activeInputMethod == valuesThenCell && valuesThenCell.isValuesEmpty()) {
			setUndecided();
		}
	}

	public void onSweep() {
		if (target.getMarkedPosition() != null) {
			lastMarkedPosition = target.getMarkedPosition();
		}

		if (activeInputMethod != null) {
			activeInputMethod.onSweep();
		}
	}

	public void onTap(Position position, boolean editable) {
		if ((activeInputMethod == null || activeInputMethod == cellThenValues)
				&& (position == null || position.equals(lastMarkedPosition))) {
			setUndecided();
		}
		else {
			ifUndecidedUseCellThenValues();

			activeInputMethod.onTap(position, editable);
		}
	}

	public void onValuesChanged() {
		if (activeInputMethod != null)
			activeInputMethod.onValuesChanged();
	}

	private void setUndecided() {
		activeInputMethod = null;
		target.setMarkedPosition(null);
		target.highlightDigit(null);
		lastMarkedPosition = null;
	}

	private void ifUndecidedUseValuesThenCells() {
		if (activeInputMethod == null) {
			activeInputMethod = valuesThenCell;
		}
	}

	private void ifUndecidedUseCellThenValues() {
		if (activeInputMethod == null) {
			activeInputMethod = cellThenValues;
		}
	}
}

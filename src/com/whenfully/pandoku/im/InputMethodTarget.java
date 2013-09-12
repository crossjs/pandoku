package com.whenfully.pandoku.im;

import com.whenfully.pandoku.model.Position;
import com.whenfully.pandoku.model.ValueSet;

public interface InputMethodTarget {
	int getPuzzleSize();

	Position getMarkedPosition();
	void setMarkedPosition(Position position);

	boolean isClue(Position position);

	ValueSet getCellValues(Position position);
	void setCellValues(Position position, ValueSet values);

	int getNumberOfDigitButtons();
	void checkButton(int digit, boolean checked);

	void highlightDigit(Integer digit);
}

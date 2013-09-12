package com.whenfully.pandoku.im;

import android.os.Bundle;

import com.whenfully.pandoku.model.Position;

public interface InputMethod {
	void onSaveInstanceState(Bundle outState);
	void onRestoreInstanceState(Bundle savedInstanceState);

	void reset();

	void onMoveMark(int dy, int dx);
	void onKeypad(int digit);
	void onClear();
	void onInvert();
	void onSweep();
	void onTap(Position position, boolean editable);

	void onValuesChanged();
}

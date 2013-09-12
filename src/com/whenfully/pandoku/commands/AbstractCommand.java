package com.whenfully.pandoku.commands;

import com.whenfully.pandoku.history.Command;
import com.whenfully.pandoku.model.PandokuPuzzle;
import com.whenfully.pandoku.model.ValueSet;

public abstract class AbstractCommand implements Command<PandokuContext> {
	protected AbstractCommand() {
	}

	public boolean isEffective() {
		return true;
	}

	public Command<PandokuContext> mergeDown(Command<PandokuContext> last) {
		return null;
	}

	public int describeContents() {
		return 0;
	}

	protected ValueSet[][] saveValues(PandokuPuzzle puzzle) {
		final int size = puzzle.getSize();
		ValueSet[][] result = new ValueSet[size][size];

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				result[row][col] = puzzle.getValues(row, col);

		return result;
	}

	protected void restoreValues(PandokuPuzzle puzzle, ValueSet[][] originalValues) {
		final int size = puzzle.getSize();

		for (int row = 0; row < size; row++)
			for (int col = 0; col < size; col++)
				puzzle.setValues(row, col, originalValues[row][col]);
	}
}

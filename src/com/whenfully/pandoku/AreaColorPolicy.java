package com.whenfully.pandoku;

import java.util.EnumSet;

import com.whenfully.pandoku.model.PuzzleType;

public enum AreaColorPolicy {
	NEVER(EnumSet.noneOf(PuzzleType.class)), //
	STANDARD_SQUIGGLY(EnumSet.of(PuzzleType.STANDARD, PuzzleType.SQUIGGLY)), //
	// name does not include PERCENT but cannot be changed because it is used in existing user preferences
	STANDARD_X_HYPER_SQUIGGLY(EnumSet.of(PuzzleType.STANDARD, PuzzleType.STANDARD_X,
			PuzzleType.STANDARD_HYPER, PuzzleType.STANDARD_PERCENT, PuzzleType.SQUIGGLY)), //
	// all except color sudoku
	ALWAYS(EnumSet.of(PuzzleType.STANDARD, PuzzleType.STANDARD_X, PuzzleType.STANDARD_HYPER,
			PuzzleType.STANDARD_PERCENT, PuzzleType.SQUIGGLY, PuzzleType.SQUIGGLY_X,
			PuzzleType.SQUIGGLY_HYPER, PuzzleType.SQUIGGLY_PERCENT));

	private final EnumSet<PuzzleType> puzzleTypes;

	AreaColorPolicy(EnumSet<PuzzleType> puzzleTypes) {
		this.puzzleTypes = puzzleTypes;
	}

	public boolean matches(PuzzleType puzzleType) {
		return puzzleTypes.contains(puzzleType);
	}
}

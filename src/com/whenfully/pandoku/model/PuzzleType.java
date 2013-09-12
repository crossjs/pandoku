package com.whenfully.pandoku.model;

public enum PuzzleType {
	// do not change order of constants - used in db
	STANDARD,
	STANDARD_X,
	STANDARD_HYPER,
	SQUIGGLY,
	SQUIGGLY_X,
	SQUIGGLY_HYPER,
	STANDARD_PERCENT,
	SQUIGGLY_PERCENT,
	STANDARD_COLOR,
	SQUIGGLY_COLOR;

	public static PuzzleType forOrdinal(int ordinal) {
		return PuzzleType.values()[ordinal];
	}
}

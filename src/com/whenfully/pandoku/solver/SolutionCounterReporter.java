package com.whenfully.pandoku.solver;

import com.whenfully.pandoku.model.Puzzle;

/**
 * Puzzle reporter that counts the number of solutions of a sudoku puzzle.
 */
public final class SolutionCounterReporter implements PuzzleReporter {
	private long counter;

	public boolean report(Puzzle solution) {
		counter++;
		return true;
	}

	public long getCounter() {
		return counter;
	}
}
